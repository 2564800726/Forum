package com.blogofyb.forum.utils.constant;

public class ServerInformation {
    public static final String ADDRESS = "http://129.204.3.245:13524/";
    // 登陆
    public static final String LOGIN = ADDRESS + "login";

    // 注册
    public static final String REGISTER = ADDRESS + "register";

    // 找回密码
    public static final String FIND_PASSWORD = ADDRESS + "findPassword";

    // 获取验证码
    public static final String GET_VERIFICATION_CODE = ADDRESS + "getVerificationCode";

    // 获取帖子
    public static final String GET_POSTS = ADDRESS + "posts?plate=";

    // 获取板块（有账号）
    public static final String GET_PLATES_WITH_ACCOUNT = ADDRESS + "plates?account=";

    // 获取板块（游客）
    public static final String GET_PLATES_WITHOUT_ACCOUNT = ADDRESS + "plates";

    // 获取置顶推荐
    public static final String GET_RECOMMEND_POST = ADDRESS + "recommend";

    // 获取指定板块中的置顶推荐
    public static final String GET_RECOMMEND_POST_WITH_PLATE = ADDRESS + "recommend?plate=";

    // 签到
    public static final String SIGN_IN = ADDRESS + "signIn";

    // 检查是否签到
    public static final String CHECK_SIGN_IN = ADDRESS + "checkSignIn";

    // 板块信息
    public static final String GET_PLATE_INFORMATION = ADDRESS + "plateInformation?id=";

    // 获取用户信息
    public static final String GET_USER_INFORMATION = ADDRESS + "userInformation?account=";

    // 修改用户信息
    public static final String UPDATE_USER_INFORMATION = ADDRESS + "updateInformation";

    // 收藏帖子
    public static final String STAR_POST = ADDRESS + "starPost";

    // 检查是否收藏
    public static final String CHECK_STAR = ADDRESS + "checkStar";

    // 获取帖子的详情
    public static final String GET_POST_DETAIL = ADDRESS + "post?id=";

    // 点赞帖子
    public static final String PRAISE_POST = ADDRESS + "praisePost";

    // 检查用户是否已经点赞
    public static final String CHECK_PRAISE = ADDRESS + "checkPraise";

    // 获取帖子的总页数
    public static final String GET_POST_TOTAL_PAGES = ADDRESS + "totalPages?id=";

    // 提交评论
    public static final String POST_COMMENT = ADDRESS + "comment";

    // 发表帖子
    public static final String POST_POST = ADDRESS + "post";

    // 订阅的用户
    public static final String SUBSCRIBE_USER = ADDRESS + "subscribe?account=";

    // 获取已收藏的帖子
    public static final String STAR = ADDRESS + "star?account=";

    // 获取粉丝列表
    public static final String FANS = ADDRESS + "fans?account=";

    // 检查是否已经关注
    public static final String CHECK_SUBSCRIBE = ADDRESS + "checkSubscribe";

    // 搜索帖子
    public static final String SEARCH = ADDRESS + "search?target=";

    // 订阅
    public static final String SUBSCRIBE = ADDRESS + "subscribe";

    // 用户的帖子
    public static final String USER_POSTS = ADDRESS + "posts?account=";

    // 关注的用户的帖子
    public static final String SUBSCRIBE_POSTS = ADDRESS + "subscribe?subscribe=";

    public static final String SUCCESS = "200";
}
