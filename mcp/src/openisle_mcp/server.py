"""Entry point for running the OpenIsle MCP server."""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from typing import Annotated, Any

import httpx
from mcp.server.fastmcp import Context, FastMCP
from pydantic import ValidationError
from pydantic import Field as PydanticField
from weakref import WeakKeyDictionary

from .config import get_settings
from .schemas import (
    CommentCreateResult,
    CommentData,
    CommentReplyResult,
    NotificationData,
    UnreadNotificationsResponse,
    PostDetail,
    PostSummary,
    RecentPostsResponse,
    SearchResponse,
    SearchResultItem,
)
from .search_client import SearchClient

settings = get_settings()
if not logging.getLogger().handlers:
    logging.basicConfig(
        level=getattr(logging, settings.log_level.upper(), logging.INFO),
        format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
    )
else:
    logging.getLogger().setLevel(
        getattr(logging, settings.log_level.upper(), logging.INFO)
    )

logger = logging.getLogger(__name__)

search_client = SearchClient(
    str(settings.backend_base_url),
    timeout=settings.request_timeout,
    access_token=(
        settings.access_token.get_secret_value()
        if settings.access_token is not None
        else None
    ),
)


class SessionTokenManager:
    """Cache JWT access tokens on a per-session basis."""

    def __init__(self) -> None:
        self._tokens: WeakKeyDictionary[Any, str] = WeakKeyDictionary()

    def resolve(
        self, ctx: Context | None, token: str | None = None
    ) -> str | None:
        """Resolve and optionally persist the token for the current session."""

        session = self._get_session(ctx)

        if isinstance(token, str):
            stripped = token.strip()
            if stripped:
                if session is not None:
                    self._tokens[session] = stripped
                    logger.debug(
                        "Stored JWT token for session %s.",
                        self._describe_session(session),
                    )
                return stripped

            if session is not None and session in self._tokens:
                logger.debug(
                    "Clearing stored JWT token for session %s due to empty input.",
                    self._describe_session(session),
                )
                del self._tokens[session]
            return None

        if session is not None:
            cached = self._tokens.get(session)
            if cached:
                logger.debug(
                    "Reusing cached JWT token for session %s.",
                    self._describe_session(session),
                )
            return cached

        return None

    @staticmethod
    def _get_session(ctx: Context | None) -> Any | None:
        if ctx is None:
            return None
        try:
            return ctx.session
        except Exception:  # pragma: no cover - defensive guard
            return None

    @staticmethod
    def _describe_session(session: Any) -> str:
        identifier = getattr(session, "mcp_session_id", None)
        if isinstance(identifier, str) and identifier:
            return identifier
        return hex(id(session))


session_token_manager = SessionTokenManager()


@asynccontextmanager
async def lifespan(_: FastMCP):
    """Lifecycle hook that disposes shared resources when the server stops."""

    try:
        logger.debug("OpenIsle MCP server lifespan started.")
        yield
    finally:
        logger.debug("Disposing shared SearchClient instance.")
        await search_client.aclose()


app = FastMCP(
    name="openisle-mcp",
    instructions=(
        "Use this server to search OpenIsle content, reply to posts and comments with "
        "session-managed authentication, retrieve details for a specific post, list posts created "
        "within a recent time window, and review unread notification messages."
    ),
    host=settings.host,
    port=settings.port,
    lifespan=lifespan,
)


@app.tool(
    name="search",
    description="Perform a global search across OpenIsle resources.",
    structured_output=True,
)
async def search(
    keyword: Annotated[str, PydanticField(description="Keyword to search for.")],
    ctx: Context | None = None,
) -> SearchResponse:
    """Call the OpenIsle global search endpoint and return structured results."""

    sanitized = keyword.strip()
    if not sanitized:
        raise ValueError("Keyword must not be empty.")

    try:
        logger.info("Received search request for keyword='%s'", sanitized)
        raw_results = await search_client.global_search(sanitized)
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        message = (
            "OpenIsle backend returned HTTP "
            f"{exc.response.status_code} while searching for '{sanitized}'."
        )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = f"Unable to reach OpenIsle backend search service: {exc}."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        results = [SearchResultItem.model_validate(entry) for entry in raw_results]
    except ValidationError as exc:
        message = "Received malformed data from the OpenIsle backend search endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    if ctx is not None:
        await ctx.info(f"Search keyword '{sanitized}' returned {len(results)} results.")
    logger.debug(
        "Validated %d search results for keyword='%s'",
        len(results),
        sanitized,
    )

    return SearchResponse(keyword=sanitized, total=len(results), results=results)


@app.tool(
    name="reply_to_post",
    description="Create a comment on a post using session authentication.",
    structured_output=True,
)
async def reply_to_post(
    post_id: Annotated[
        int,
        PydanticField(ge=1, description="Identifier of the post being replied to."),
    ],
    content: Annotated[
        str,
        PydanticField(description="Markdown content of the reply."),
    ],
    captcha: Annotated[
        str | None,
        PydanticField(
            default=None,
            description="Optional captcha solution if the backend requires it.",
        ),
    ] = None,
    ctx: Context | None = None,
) -> CommentCreateResult:
    """Create a comment on a post and return the backend payload."""

    sanitized_content = content.strip()
    if not sanitized_content:
        raise ValueError("Reply content must not be empty.")

    sanitized_captcha = captcha.strip() if isinstance(captcha, str) else None

    resolved_token = session_token_manager.resolve(ctx)

    try:
        logger.info(
            "Creating reply for post_id=%s (captcha=%s)",
            post_id,
            bool(sanitized_captcha),
        )
        raw_comment = await search_client.reply_to_post(
            post_id,
            resolved_token,
            sanitized_content,
            sanitized_captcha,
        )
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        status_code = exc.response.status_code
        if status_code == 401:
            message = (
                "Authentication failed while replying to post "
                f"{post_id}. Please verify the token."
            )
        elif status_code == 403:
            message = (
                "The provided token is not authorized to reply to post "
                f"{post_id}."
            )
        elif status_code == 404:
            message = f"Post {post_id} was not found."
        else:
            message = (
                "OpenIsle backend returned HTTP "
                f"{status_code} while replying to post {post_id}."
            )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = (
            "Unable to reach OpenIsle backend comment service: "
            f"{exc}."
        )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        comment = CommentData.model_validate(raw_comment)
    except ValidationError as exc:
        message = "Received malformed data from the post comment endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    if ctx is not None:
        await ctx.info(
            "Reply created successfully for post "
            f"{post_id}."
        )
    logger.debug(
        "Validated reply comment payload for post_id=%s (comment_id=%s)",
        post_id,
        comment.id,
    )

    return CommentCreateResult(comment=comment)


@app.tool(
    name="reply_to_comment",
    description="Reply to an existing comment using session authentication.",
    structured_output=True,
)
async def reply_to_comment(
    comment_id: Annotated[
        int,
        PydanticField(ge=1, description="Identifier of the comment being replied to."),
    ],
    content: Annotated[
        str,
        PydanticField(description="Markdown content of the reply."),
    ],
    captcha: Annotated[
        str | None,
        PydanticField(
            default=None,
            description="Optional captcha solution if the backend requires it.",
        ),
    ] = None,
    ctx: Context | None = None,
) -> CommentReplyResult:
    """Create a reply for a comment and return the backend payload."""

    sanitized_content = content.strip()
    if not sanitized_content:
        raise ValueError("Reply content must not be empty.")

    sanitized_captcha = captcha.strip() if isinstance(captcha, str) else None

    resolved_token = session_token_manager.resolve(ctx)

    try:
        logger.info(
            "Creating reply for comment_id=%s (captcha=%s)",
            comment_id,
            bool(sanitized_captcha),
        )
        raw_comment = await search_client.reply_to_comment(
            comment_id,
            resolved_token,
            sanitized_content,
            sanitized_captcha,
        )
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        status_code = exc.response.status_code
        if status_code == 401:
            message = (
                "Authentication failed while replying to comment "
                f"{comment_id}. Please verify the token."
            )
        elif status_code == 403:
            message = (
                "The provided token is not authorized to reply to comment "
                f"{comment_id}."
            )
        else:
            message = (
                "OpenIsle backend returned HTTP "
                f"{status_code} while replying to comment {comment_id}."
            )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = (
            "Unable to reach OpenIsle backend comment service: "
            f"{exc}."
        )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        comment = CommentData.model_validate(raw_comment)
    except ValidationError as exc:
        message = "Received malformed data from the reply comment endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    if ctx is not None:
        await ctx.info(
            "Reply created successfully for comment "
            f"{comment_id}."
        )
    logger.debug(
        "Validated reply payload for comment_id=%s (reply_id=%s)",
        comment_id,
        comment.id,
    )

    return CommentReplyResult(comment=comment)


@app.tool(
    name="recent_posts",
    description="Retrieve posts created in the last N minutes.",
    structured_output=True,
)
async def recent_posts(
    minutes: Annotated[
        int,
        PydanticField(gt=0, le=1440, description="Time window in minutes to search for new posts."),
    ],
    ctx: Context | None = None,
) -> RecentPostsResponse:
    """Fetch recent posts from the backend and return structured data."""

    try:
        logger.info("Fetching recent posts for last %s minutes", minutes)
        raw_posts = await search_client.recent_posts(minutes)
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        message = (
            "OpenIsle backend returned HTTP "
            f"{exc.response.status_code} while fetching recent posts for the last {minutes} minutes."
        )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = f"Unable to reach OpenIsle backend recent posts service: {exc}."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        posts = [PostSummary.model_validate(entry) for entry in raw_posts]
    except ValidationError as exc:
        message = "Received malformed data from the recent posts endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    if ctx is not None:
        await ctx.info(
            f"Found {len(posts)} posts created within the last {minutes} minutes."
        )
    logger.debug(
        "Validated %d recent posts for window=%s minutes",
        len(posts),
        minutes,
    )

    return RecentPostsResponse(minutes=minutes, total=len(posts), posts=posts)


@app.tool(
    name="get_post",
    description="Retrieve detailed information for a single post.",
    structured_output=True,
)
async def get_post(
    post_id: Annotated[
        int,
        PydanticField(ge=1, description="Identifier of the post to retrieve."),
    ],
    ctx: Context | None = None,
) -> PostDetail:
    """Fetch post details from the backend and validate the response."""

    resolved_token = session_token_manager.resolve(ctx)

    try:
        logger.info("Fetching post details for post_id=%s", post_id)
        raw_post = await search_client.get_post(post_id, resolved_token)
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        status_code = exc.response.status_code
        if status_code == 404:
            message = f"Post {post_id} was not found."
        elif status_code == 401:
            message = "Authentication failed while retrieving the post."
        elif status_code == 403:
            message = "The provided token is not authorized to view this post."
        else:
            message = (
                "OpenIsle backend returned HTTP "
                f"{status_code} while retrieving post {post_id}."
            )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = f"Unable to reach OpenIsle backend post service: {exc}."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        post = PostDetail.model_validate(raw_post)
    except ValidationError as exc:
        message = "Received malformed data from the post detail endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    if ctx is not None:
        await ctx.info(f"Retrieved post {post_id} successfully.")
    logger.debug(
        "Validated post payload for post_id=%s with %d comments",
        post_id,
        len(post.comments),
    )

    return post


@app.tool(
    name="list_unread_messages",
    description="List unread notification messages for the authenticated user.",
    structured_output=True,
)
async def list_unread_messages(
    page: Annotated[
        int,
        PydanticField(
            default=0,
            ge=0,
            description="Page number of unread notifications to retrieve.",
        ),
    ] = 0,
    size: Annotated[
        int,
        PydanticField(
            default=30,
            ge=1,
            le=100,
            description="Number of unread notifications to include per page.",
        ),
    ] = 30,
    ctx: Context | None = None,
) -> UnreadNotificationsResponse:
    """Retrieve unread notifications and return structured data."""

    resolved_token = session_token_manager.resolve(ctx)

    try:
        logger.info(
            "Fetching unread notifications (page=%s, size=%s)",
            page,
            size,
        )
        raw_notifications = await search_client.list_unread_notifications(
            page=page,
            size=size,
            token=resolved_token,
        )
    except httpx.HTTPStatusError as exc:  # pragma: no cover - network errors
        message = (
            "OpenIsle backend returned HTTP "
            f"{exc.response.status_code} while fetching unread notifications."
        )
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc
    except httpx.RequestError as exc:  # pragma: no cover - network errors
        message = f"Unable to reach OpenIsle backend notification service: {exc}."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    try:
        notifications = [
            NotificationData.model_validate(entry) for entry in raw_notifications
        ]
    except ValidationError as exc:
        message = "Received malformed data from the unread notifications endpoint."
        if ctx is not None:
            await ctx.error(message)
        raise ValueError(message) from exc

    total = len(notifications)
    if ctx is not None:
        await ctx.info(
            f"Retrieved {total} unread notifications (page {page}, size {size})."
        )
    logger.debug(
        "Validated %d unread notifications for page=%s size=%s",
        total,
        page,
        size,
    )

    return UnreadNotificationsResponse(
        page=page,
        size=size,
        total=total,
        notifications=notifications,
    )


def main() -> None:
    """Run the MCP server using the configured transport."""

    app.run(transport=settings.transport)


if __name__ == "__main__":  # pragma: no cover - manual execution
    main()

