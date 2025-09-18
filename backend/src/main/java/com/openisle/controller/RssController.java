package com.openisle.controller;

import com.openisle.model.Comment;
import com.openisle.model.CommentSort;
import com.openisle.model.Post;
import com.openisle.service.CommentService;
import com.openisle.service.PostService;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RssController {

  private final PostService postService;
  private final CommentService commentService;

  @Value("${app.website-url:https://www.open-isle.com}")
  private String websiteUrl;

  // 兼容 Markdown/HTML 两类图片写法（用于 enclosure）
  private static final Pattern MD_IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");
  private static final Pattern HTML_IMAGE = Pattern.compile(
    "<BaseImage[^>]+src=[\"']?([^\"'>]+)[\"']?[^>]*>"
  );

  private static final DateTimeFormatter RFC1123 = DateTimeFormatter.RFC_1123_DATE_TIME;

  // flexmark：Markdown -> HTML
  private static final Parser MD_PARSER;
  private static final HtmlRenderer MD_RENDERER;

  static {
    MutableDataSet opts = new MutableDataSet();
    opts.set(
      Parser.EXTENSIONS,
      Arrays.asList(
        TablesExtension.create(),
        AutolinkExtension.create(),
        StrikethroughExtension.create(),
        TaskListExtension.create()
      )
    );
    // 允许内联 HTML（下游再做 sanitize）
    opts.set(Parser.HTML_BLOCK_PARSER, true);
    MD_PARSER = Parser.builder(opts).build();
    MD_RENDERER = HtmlRenderer.builder(opts).escapeHtml(false).build();
  }

  @GetMapping(value = "/api/rss", produces = "application/rss+xml;charset=UTF-8")
  @Operation(summary = "RSS feed", description = "Generate RSS feed for latest posts")
  @ApiResponse(
    responseCode = "200",
    description = "RSS XML",
    content = @Content(schema = @Schema(implementation = String.class))
  )
  public String feed() {
    // 建议 20；你现在是 10，这里保留你的 10
    List<Post> posts = postService.listLatestRssPosts(10);
    String base = trimTrailingSlash(websiteUrl);

    StringBuilder sb = new StringBuilder(4096);
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<rss version=\"2.0\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">");
    sb.append("<channel>");
    elem(sb, "title", cdata("OpenIsle RSS"));
    elem(sb, "link", base + "/");
    elem(sb, "description", cdata("Latest posts"));
    ZonedDateTime updated = posts
      .stream()
      .map(p -> p.getCreatedAt().atZone(ZoneId.systemDefault()))
      .max(Comparator.naturalOrder())
      .orElse(ZonedDateTime.now());
    // channel lastBuildDate（GMT）
    elem(sb, "lastBuildDate", toRfc1123Gmt(updated));

    for (Post p : posts) {
      String link = base + "/posts/" + p.getId();

      // 1) Markdown -> HTML
      String html = renderMarkdown(p.getContent());

      // 2) Sanitize（白名单增强）
      String safeHtml = sanitizeHtml(html);

      // 3) 绝对化 href/src + 强制 rel/target
      String absHtml = absolutifyHtml(safeHtml, base);

      // 4) 纯文本摘要（用于 <description>）
      String plain = textSummary(absHtml, 180);

      // 5) enclosure（首图，已绝对化）
      String enclosure = firstImage(p.getContent());
      if (enclosure == null) {
        // 如果 Markdown 没有图，尝试从渲染后的 HTML 再抓一次
        enclosure = firstImage(absHtml);
      }
      if (enclosure != null) {
        enclosure = absolutifyUrl(enclosure, base);
      }

      // 6) 构造优雅的附加区块（原文链接 + 精选评论），编入 <content:encoded>
      List<Comment> topComments = commentService.getCommentsForPost(
        p.getId(),
        CommentSort.MOST_INTERACTIONS
      );
      topComments = topComments.subList(0, Math.min(10, topComments.size()));
      String footerHtml = buildFooterHtml(base, link, topComments);

      sb.append("<item>");
      elem(sb, "title", cdata(nullSafe(p.getTitle())));
      elem(sb, "link", link);
      sb.append("<guid isPermaLink=\"true\">").append(link).append("</guid>");
      elem(sb, "pubDate", toRfc1123Gmt(p.getCreatedAt().atZone(ZoneId.systemDefault())));
      // 摘要
      elem(sb, "description", cdata(plain));
      // 全文（HTML）：正文 + 优雅的 Markdown 区块（已转 HTML）
      sb
        .append("<content:encoded><![CDATA[")
        .append(absHtml)
        .append(footerHtml)
        .append("]]></content:encoded>");
      // 首图 enclosure（图片类型）
      if (enclosure != null) {
        sb
          .append("<enclosure url=\"")
          .append(escapeXml(enclosure))
          .append("\" type=\"")
          .append(getMimeType(enclosure))
          .append("\" />");
      }
      sb.append("</item>");
    }

    sb.append("</channel></rss>");
    return sb.toString();
  }

  /* ===================== Markdown → HTML ===================== */

  private static String renderMarkdown(String md) {
    if (md == null || md.isEmpty()) return "";
    return MD_RENDERER.render(MD_PARSER.parse(md));
  }

  /* ===================== Sanitize & 绝对化 ===================== */

  private static String sanitizeHtml(String html) {
    if (html == null) return "";
    Safelist wl = Safelist.relaxed()
      .addTags(
        "pre",
        "code",
        "figure",
        "figcaption",
        "picture",
        "source",
        "table",
        "thead",
        "tbody",
        "tr",
        "th",
        "td",
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "hr",
        "blockquote"
      )
      .addAttributes("a", "href", "title", "target", "rel")
      .addAttributes("img", "src", "alt", "title", "width", "height")
      .addAttributes("source", "srcset", "type", "media")
      .addAttributes("code", "class")
      .addAttributes("pre", "class")
      .addProtocols("a", "href", "http", "https", "mailto")
      .addProtocols("img", "src", "http", "https", "data")
      .addProtocols("source", "srcset", "http", "https");
    // 清除所有 on* 事件、style（避免阅读器环境差异）
    return Jsoup.clean(html, wl);
  }

  private static String absolutifyHtml(String html, String baseUrl) {
    if (html == null || html.isEmpty()) return "";
    Document doc = Jsoup.parseBodyFragment(html, baseUrl);
    // a[href]
    for (Element a : doc.select("a[href]")) {
      String href = a.attr("href");
      String abs = absolutifyUrl(href, baseUrl);
      a.attr("href", abs);
      // 强制外链安全属性
      a.attr("rel", "noopener noreferrer nofollow");
      a.attr("target", "_blank");
    }
    // img[src]
    for (Element img : doc.select("img[src]")) {
      String src = img.attr("src");
      String abs = absolutifyUrl(src, baseUrl);
      img.attr("src", abs);
    }
    // source[srcset] （picture/webp）
    for (Element s : doc.select("source[srcset]")) {
      String srcset = s.attr("srcset");
      s.attr("srcset", absolutifySrcset(srcset, baseUrl));
    }
    return doc.body().html();
  }

  private static String absolutifyUrl(String url, String baseUrl) {
    if (url == null || url.isEmpty()) return url;
    String u = url.trim();
    if (u.startsWith("//")) {
      return "https:" + u;
    }
    if (u.startsWith("#")) {
      // 保留页面内锚点：拼接到首页（也可拼接到当前帖子的 link，但此处无上下文）
      return baseUrl + "/" + u;
    }
    try {
      URI base = URI.create(ensureTrailingSlash(baseUrl));
      URI abs = base.resolve(u);
      return abs.toString();
    } catch (Exception e) {
      return url;
    }
  }

  private static String absolutifySrcset(String srcset, String baseUrl) {
    if (srcset == null || srcset.isEmpty()) return srcset;
    String[] parts = srcset.split(",");
    List<String> out = new ArrayList<>(parts.length);
    for (String part : parts) {
      String p = part.trim();
      if (p.isEmpty()) continue;
      String[] seg = p.split("\\s+");
      String url = seg[0];
      String size = seg.length > 1 ? seg[1] : "";
      out.add(absolutifyUrl(url, baseUrl) + (size.isEmpty() ? "" : " " + size));
    }
    return String.join(", ", out);
  }

  /* ===================== 摘要 & enclosure ===================== */

  private static String textSummary(String html, int maxLen) {
    if (html == null) return "";
    String text = Jsoup.parse(html).text().replaceAll("\\s+", " ").trim();
    if (text.length() <= maxLen) return text;
    return text.substring(0, maxLen) + "…";
  }

  private String firstImage(String content) {
    if (content == null) return null;
    Matcher m = MD_IMAGE.matcher(content);
    if (m.find()) return m.group(1);
    m = HTML_IMAGE.matcher(content);
    if (m.find()) return m.group(1);
    // 再从纯 HTML 里解析一次（如果传入的是渲染后的）
    try {
      Document doc = Jsoup.parse(content);
      Element img = doc.selectFirst("img[src]");
      if (img != null) return img.attr("src");
    } catch (Exception ignored) {}
    return null;
  }

  private static String getMimeType(String url) {
    String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".gif")) return "image/gif";
    if (lower.endsWith(".webp")) return "image/webp";
    if (lower.endsWith(".svg")) return "image/svg+xml";
    if (lower.endsWith(".avif")) return "image/avif";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    // 默认兜底
    return "image/jpeg";
  }

  /* ===================== 附加区块（原文链接 + 精选评论） ===================== */

  /**
   * 将“原文链接 + 精选评论（最多 10 条）”以优雅的 Markdown 形式渲染为 HTML，
   * 并做 sanitize + 绝对化，然后拼入 content:encoded 尾部。
   */
  private static String buildFooterHtml(
    String baseUrl,
    String originalLink,
    List<Comment> topComments
  ) {
    StringBuilder md = new StringBuilder(256);

    // 分割线
    md.append("\n\n---\n\n");

    // 原文链接（强调 + 可点击）
    md
      .append("**原文链接：** ")
      .append("[")
      .append(originalLink)
      .append("](")
      .append(originalLink)
      .append(")")
      .append("\n\n");

    // 精选评论（仅当有评论时展示）
    if (topComments != null && !topComments.isEmpty()) {
      md.append("### 精选评论（Top ").append(Math.min(10, topComments.size())).append("）\n\n");
      for (Comment c : topComments) {
        String author = usernameOf(c);
        String content = nullSafe(c.getContent()).replace("\r", "");
        // 使用引用样式展示，提升可读性
        md.append("> @").append(author).append(": ").append(content).append("\n\n");
      }
    }

    // 渲染为 HTML，并保持和正文一致的处理流程
    String html = renderMarkdown(md.toString());
    String safe = sanitizeHtml(html);
    return absolutifyHtml(safe, baseUrl);
  }

  private static String usernameOf(Comment c) {
    if (c == null) return "匿名";
    try {
      Object authorObj = c.getAuthor();
      if (authorObj == null) return "匿名";
      // 反射避免直接依赖实体字段名变化（也可直接强转到具体类型）
      String username;
      try {
        username = (String) authorObj.getClass().getMethod("getUsername").invoke(authorObj);
      } catch (Exception e) {
        username = null;
      }
      if (username == null || username.isEmpty()) return "匿名";
      return username;
    } catch (Exception ignored) {
      return "匿名";
    }
  }

  /* ===================== 时间/字符串/XML ===================== */

  private static String toRfc1123Gmt(ZonedDateTime zdt) {
    return zdt.withZoneSameInstant(ZoneId.of("GMT")).format(RFC1123);
  }

  private static String cdata(String s) {
    if (s == null) return "<![CDATA[]]>";
    // 防止出现 "]]>" 终止标记破坏 CDATA
    return "<![CDATA[" + s.replace("]]>", "]]]]><![CDATA[>") + "]]>";
  }

  private static void elem(StringBuilder sb, String name, String value) {
    sb.append('<').append(name).append('>').append(value).append("</").append(name).append('>');
  }

  private static String escapeXml(String s) {
    if (s == null) return "";
    return s
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;");
  }

  private static String trimTrailingSlash(String s) {
    if (s == null) return "";
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }

  private static String ensureTrailingSlash(String s) {
    if (s == null || s.isEmpty()) return "/";
    return s.endsWith("/") ? s : s + "/";
  }

  private static String nullSafe(String s) {
    return s == null ? "" : s;
  }
}
