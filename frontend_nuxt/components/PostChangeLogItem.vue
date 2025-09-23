<template>
  <div :id="`change-log-${log.id}`" class="change-log-container">
    <div class="change-log-text">
      <BaseImage
        v-if="log.userAvatar"
        class="change-log-avatar"
        :src="log.userAvatar"
        alt="avatar"
        @click="() => navigateTo(`/users/${log.username}`)"
      />
      <span v-if="log.username" class="change-log-user">{{ log.username }}</span>
      <span v-if="log.type === 'CONTENT'" class="change-log-content">变更了文章内容</span>
      <span v-else-if="log.type === 'TITLE'" class="change-log-content">变更了文章标题</span>
      <template v-else-if="log.type === 'CATEGORY'">
        <div class="change-log-category-text">变更了文章分类, 从</div>
        <ArticleCategory :category="log.oldCategory" />
        <div class="change-log-category-text">修改为</div>
        <ArticleCategory :category="log.newCategory" />
      </template>
      <template v-else-if="log.type === 'TAG'">
        <div class="change-log-category-text">变更了文章标签, 从</div>
        <ArticleTags :tags="log.oldTags" />
        <div class="change-log-category-text">修改为</div>
        <ArticleTags :tags="log.newTags" />
      </template>
      <span v-else-if="log.type === 'CLOSED'" class="change-log-content">
        <template v-if="log.newClosed">关闭了文章</template>
        <template v-else>重新打开了文章</template>
      </span>
      <span v-else-if="log.type === 'PINNED'" class="change-log-content">
        <template v-if="log.newPinnedAt">置顶了文章</template>
        <template v-else>取消置顶文章</template>
      </span>
      <span v-else-if="log.type === 'FEATURED'" class="change-log-content">
        <template v-if="log.newFeatured">将文章设为精选</template>
        <template v-else>取消精选文章</template>
      </span>
      <span v-else-if="log.type === 'VOTE_RESULT'" class="change-log-content"
        >系统已计算投票结果</span
      >
      <span v-else-if="log.type === 'LOTTERY_RESULT'" class="change-log-content"
        >系统已「精密计算」抽奖结果 (=ﾟωﾟ)ﾉ</span
      >
    </div>
    <div class="change-log-time">{{ log.time }}</div>
    <div
      v-if="log.type === 'CONTENT' || log.type === 'TITLE'"
      class="content-diff"
      v-html="diffHtml"
    ></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { html } from 'diff2html'
import { createTwoFilesPatch } from 'diff'
import { useIsMobile } from '~/utils/screen'
import 'diff2html/bundles/css/diff2html.min.css'
import BaseImage from '~/components/BaseImage.vue'
import { navigateTo } from 'nuxt/app'
import { themeState } from '~/utils/theme'
import ArticleCategory from '~/components/ArticleCategory.vue'
import ArticleTags from '~/components/ArticleTags.vue'
const props = defineProps({
  log: Object,
  title: String,
})

const diffHtml = computed(() => {
  // Track theme changes
  const isDark = import.meta.client && document.documentElement.dataset.theme === 'dark'
  themeState.mode
  const colorScheme = isDark ? 'dark' : 'light'

  if (props.log.type === 'CONTENT') {
    const oldContent = props.log.oldContent ?? ''
    const newContent = props.log.newContent ?? ''
    const diff = createTwoFilesPatch(props.title, props.title, oldContent, newContent)
    return html(diff, {
      inputFormat: 'diff',
      showFiles: false,
      matching: 'lines',
      drawFileList: false,
      colorScheme,
    })
  } else if (props.log.type === 'TITLE') {
    const oldTitle = props.log.oldTitle ?? ''
    const newTitle = props.log.newTitle ?? ''
    const diff = createTwoFilesPatch(oldTitle, newTitle, '', '')
    return html(diff, {
      inputFormat: 'diff',
      showFiles: false,
      matching: 'lines',
      drawFileList: false,
      colorScheme,
    })
  }
  return ''
})
</script>

<style scoped>
.change-log-container {
  display: flex;
  flex-direction: column;
  /* padding-top: 5px; */
  /* padding-bottom: 30px; */
  font-size: 14px;
  border-bottom: 1px solid var(--normal-border-color);
  padding-bottom: 10px;
}

.change-log-text {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.change-log-user {
  font-weight: bold;
  margin-right: 4px;
}

.change-log-user,
.change-log-content {
  opacity: 0.7;
}

.change-log-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 4px;
  cursor: pointer;
}

.change-log-time {
  font-size: 12px;
  opacity: 0.6;
}

.content-diff {
  margin-top: 8px;
}

.change-log-category {
  display: flex;
  flex-direction: row;
  gap: 4px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
