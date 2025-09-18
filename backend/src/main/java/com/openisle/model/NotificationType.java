package com.openisle.model;

/**
 * Types of user notifications.
 */
public enum NotificationType {
  /** Someone viewed your post */
  POST_VIEWED,
  /** Someone replied to your post or comment */
  COMMENT_REPLY,
  /** Someone reacted to your post or comment */
  REACTION,
  /** A new post is waiting for review */
  POST_REVIEW_REQUEST,
  /** Your post under review was approved or rejected */
  POST_REVIEWED,
  /** An administrator deleted your post */
  POST_DELETED,
  /** A subscribed post received a new comment */
  POST_UPDATED,
  /** Someone subscribed to your post */
  POST_SUBSCRIBED,
  /** Someone unsubscribed from your post */
  POST_UNSUBSCRIBED,
  /** Someone you follow published a new post */
  FOLLOWED_POST,
  /** Someone started following you */
  USER_FOLLOWED,
  /** Someone unfollowed you */
  USER_UNFOLLOWED,
  /** A user you subscribe to created a post or comment */
  USER_ACTIVITY,
  /** A user requested registration approval */
  REGISTER_REQUEST,
  /** A user redeemed an activity reward */
  ACTIVITY_REDEEM,
  /** A user redeemed a point good */
  POINT_REDEEM,
  /** You won a lottery post */
  LOTTERY_WIN,
  /** Your lottery post was drawn */
  LOTTERY_DRAW,
  /** Someone participated in your poll */
  POLL_VOTE,
  /** Your poll post has concluded */
  POLL_RESULT_OWNER,
  /** A poll you participated in has concluded */
  POLL_RESULT_PARTICIPANT,
  /** Your post was featured */
  POST_FEATURED,
  /** You were mentioned in a post or comment */
  MENTION,
}
