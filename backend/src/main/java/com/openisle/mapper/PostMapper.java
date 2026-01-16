package com.openisle.mapper;

import com.openisle.dto.AuthorDto;
import com.openisle.dto.CommentDto;
import com.openisle.dto.LotteryDto;
import com.openisle.dto.PollDto;
import com.openisle.dto.PostDetailDto;
import com.openisle.dto.PostSummaryDto;
import com.openisle.dto.ProposalDto;
import com.openisle.dto.ReactionDto;
import com.openisle.model.CategoryProposalPost;
import com.openisle.model.CommentSort;
import com.openisle.model.LotteryPost;
import com.openisle.model.PollPost;
import com.openisle.model.PollVote;
import com.openisle.model.Post;
import com.openisle.model.User;
import com.openisle.repository.PollVoteRepository;
import com.openisle.service.CommentService;
import com.openisle.service.ReactionService;
import com.openisle.service.SubscriptionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Mapper responsible for converting posts into DTOs. */
@Component
@RequiredArgsConstructor
public class PostMapper {

  private final CommentService commentService;
  private final ReactionService reactionService;
  private final SubscriptionService subscriptionService;
  private final CommentMapper commentMapper;
  private final ReactionMapper reactionMapper;
  private final UserMapper userMapper;
  private final TagMapper tagMapper;
  private final CategoryMapper categoryMapper;
  private final PollVoteRepository pollVoteRepository;

  public PostSummaryDto toSummaryDto(Post post) {
    PostSummaryDto dto = new PostSummaryDto();
    applyCommon(post, dto);
    return dto;
  }

  public List<PostSummaryDto> toListDtos(List<Post> posts) {
    if (posts == null || posts.isEmpty()) {
      return List.of();
    }
    Map<Long, List<User>> participantsMap = commentService.getParticipantsForPosts(posts, 5);
    return posts
      .stream()
      .map(post -> {
        PostSummaryDto dto = new PostSummaryDto();
        applyListFields(post, dto);
        List<User> participants = participantsMap.get(post.getId());
        if (participants != null) {
          dto.setParticipants(
            participants.stream().map(userMapper::toAuthorDto).collect(Collectors.toList())
          );
        } else {
          dto.setParticipants(List.of());
        }
        dto.setReactions(List.of());
        return dto;
      })
      .collect(Collectors.toList());
  }

  public PostSummaryDto toListDto(Post post) {
    PostSummaryDto dto = new PostSummaryDto();
    applyListFields(post, dto);
    dto.setParticipants(List.of());
    dto.setReactions(List.of());
    return dto;
  }

  public PostDetailDto toDetailDto(Post post, String viewer) {
    PostDetailDto dto = new PostDetailDto();
    applyCommon(post, dto);
    List<CommentDto> comments = commentService
      .getCommentsForPost(post.getId(), CommentSort.OLDEST)
      .stream()
      .map(commentMapper::toDtoWithReplies)
      .collect(Collectors.toList());
    dto.setComments(comments);
    dto.setSubscribed(viewer != null && subscriptionService.isPostSubscribed(viewer, post.getId()));
    return dto;
  }

  private void applyListFields(Post post, PostSummaryDto dto) {
    dto.setId(post.getId());
    dto.setTitle(post.getTitle());
    dto.setContent(post.getContent());
    dto.setCreatedAt(post.getCreatedAt());
    dto.setAuthor(userMapper.toAuthorDto(post.getAuthor()));
    dto.setCategory(categoryMapper.toDto(post.getCategory()));
    dto.setTags(post.getTags().stream().map(tagMapper::toDto).collect(Collectors.toList()));
    dto.setViews(post.getViews());
    dto.setCommentCount(post.getCommentCount());
    dto.setStatus(post.getStatus());
    dto.setPinnedAt(post.getPinnedAt());
    dto.setLastReplyAt(post.getLastReplyAt());
    dto.setRssExcluded(post.getRssExcluded() == null || post.getRssExcluded());
    dto.setClosed(post.isClosed());
    dto.setVisibleScope(post.getVisibleScope());
    dto.setType(post.getType());
  }

  private void applyCommon(Post post, PostSummaryDto dto) {
    dto.setId(post.getId());
    dto.setTitle(post.getTitle());
    dto.setContent(post.getContent());

    dto.setCreatedAt(post.getCreatedAt());
    dto.setAuthor(userMapper.toAuthorDto(post.getAuthor()));
    dto.setCategory(categoryMapper.toDto(post.getCategory()));
    dto.setTags(post.getTags().stream().map(tagMapper::toDto).collect(Collectors.toList()));
    dto.setViews(post.getViews());
    dto.setStatus(post.getStatus());
    dto.setPinnedAt(post.getPinnedAt());
    dto.setRssExcluded(post.getRssExcluded() == null || post.getRssExcluded());
    dto.setClosed(post.isClosed());
    dto.setVisibleScope(post.getVisibleScope());

    List<ReactionDto> reactions = reactionService
      .getReactionsForPost(post.getId())
      .stream()
      .map(reactionMapper::toDto)
      .collect(Collectors.toList());
    dto.setReactions(reactions);

    List<User> participants = commentService.getParticipants(post.getId(), 5);
    dto.setParticipants(
      participants.stream().map(userMapper::toAuthorDto).collect(Collectors.toList())
    );

    LocalDateTime last = post.getLastReplyAt();
    if (last == null) {
      commentService.updatePostCommentStats(post);
    }
    dto.setCommentCount(post.getCommentCount());
    dto.setLastReplyAt(post.getLastReplyAt());
    dto.setReward(0);
    dto.setSubscribed(false);
    dto.setType(post.getType());

    if (post instanceof LotteryPost lp) {
      LotteryDto l = new LotteryDto();
      l.setPrizeDescription(lp.getPrizeDescription());
      l.setPrizeIcon(lp.getPrizeIcon());
      l.setPrizeCount(lp.getPrizeCount());
      l.setPointCost(lp.getPointCost());
      l.setStartTime(lp.getStartTime());
      l.setEndTime(lp.getEndTime());
      l.setParticipants(
        lp.getParticipants().stream().map(userMapper::toAuthorDto).collect(Collectors.toList())
      );
      l.setWinners(
        lp.getWinners().stream().map(userMapper::toAuthorDto).collect(Collectors.toList())
      );
      dto.setLottery(l);
    }

    if (post instanceof CategoryProposalPost cp) {
      ProposalDto proposalDto = (ProposalDto) buildPollDto(cp, new ProposalDto());
      proposalDto.setProposalStatus(cp.getProposalStatus());
      proposalDto.setProposedName(cp.getProposedName());
      proposalDto.setDescription(cp.getDescription());
      proposalDto.setApproveThreshold(cp.getApproveThreshold());
      proposalDto.setQuorum(cp.getQuorum());
      proposalDto.setStartAt(cp.getStartAt());
      proposalDto.setResultSnapshot(cp.getResultSnapshot());
      proposalDto.setRejectReason(cp.getRejectReason());
      dto.setPoll(proposalDto);
    } else if (post instanceof PollPost pp) {
      dto.setPoll(buildPollDto(pp, new PollDto()));
    }
  }

  private PollDto buildPollDto(PollPost pollPost, PollDto target) {
    target.setOptions(pollPost.getOptions());
    target.setVotes(pollPost.getVotes());
    target.setEndTime(pollPost.getEndTime());
    target.setParticipants(
      pollPost.getParticipants().stream().map(userMapper::toAuthorDto).collect(Collectors.toList())
    );
    Map<Integer, List<AuthorDto>> optionParticipants = pollVoteRepository
      .findByPostId(pollPost.getId())
      .stream()
      .collect(
        Collectors.groupingBy(
          PollVote::getOptionIndex,
          Collectors.mapping(v -> userMapper.toAuthorDto(v.getUser()), Collectors.toList())
        )
      );
    target.setOptionParticipants(optionParticipants);
    target.setMultiple(Boolean.TRUE.equals(pollPost.getMultiple()));
    return target;
  }
}
