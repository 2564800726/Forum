## 某论坛

用到的第三方框架:

>> [me.jessyan:autosize:1.1.2](https://github.com/JessYanCoding/AndroidAutoSize)
>>
>> [com.jakewharton:disklrucache:2.0.2](https://github.com/JakeWharton/DiskLruCache)
>>
>> [de.hdodenhof:circleimageview:3.0.0](https://github.com/hdodenhof/CircleImageView)
>>
>> [com.soundcloud.android:android-crop:1.0.1@aar](https://github.com/jdamcd/android-crop)

验证码随便填写一个就可以了，不会验证

* [主页](#select)
  * [登陆](#login)
  * [注册](#register)
* [论坛](#forum)
  * [首页](#main)
  * [订阅板块](#subscribe_plate)
  * [搜索](#search_main)
  * [置顶推荐](#recommend_main)
  * [板块详情](#plate)
    * [签到](#sign_in)
    * [搜索](#search_plate)
    * [发表帖子](#edit_post)
  * [帖子详情](#post)
* [关注](#subscribe_post)
* [空间](#zone)
  * [修改资料](#change_user_information)
  * [修改空间背景](#change_background)
  * [修改头像](#change_head)
  * [我的帖子](#my_post)
  * [关注、粉丝、收藏](#count)
  * [我的回复](#my_response)
  * [设置](#setting)
    * ~~[消息设置](#message_setting)~~
    * [退出登陆](#logout)
* ~~[消息](#message)~~
* [ForumServer](#forum_server)
* [ForumImageServer](#forum_image_server)

<h3 id="forum">论坛</h3>

> > <h4 id="main">首页</h4>
> >
> > ![首页](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-45-58-959_com.blogofyb.f.png?raw=true)
> >
> > <h4 id="subscribe_plate">订阅板块</h4>
> >
> > 可以订阅和取消订阅各个板块
> >
> > ![订阅板块](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-46-06-483_com.blogofyb.f.png?raw=true)
> >
> > <h4 id="search_main">搜索</h4>
> >
> > 可以根据输入的关键字查询板块（搜索结果为标题包含该关键字的帖子）
> >
> > ![搜索]()
> >
> > <h4 id="recommend_main">置顶推荐</h4>
> >
> > 按照最后编辑的时间推荐一条最新的帖子
> >
> > <h4 id="#plate">板块详情</h4>
> >
> > 最上面的四条置顶也按了时间来推荐，和下面的帖子重复了...
> >
> > 签到可以+50exp
> >
> > ![板块](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-31-171_com.blogofyb.f.png?raw=true)
> >
> > <h4 id="post">帖子详情</h4>
> >
> > 只有楼主的第一条消息才有图片，最多只有一张，还不能大图查看...
> >
> > 点赞、收藏、翻页（没有页码跳转）、评论
> >
> > 点击楼层可以对当层主发起评论（楼主除外）
> >
> > 点击评论按钮都是对楼主的评论
> >
> > 评论exp+5
> >
> > ![帖子详情](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-41-963_com.blogofyb.f.png?raw=true)

<h3 id="subscribe_post">关注</h3>

展示出关注的人发表的帖子, 下面这个是服务器上的图片没了...

![关注的人发的帖子](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-46-14-085_com.blogofyb.f.png?raw=true)

<h3 id="zone">空间</h3>

展示出当前用户的基本信息，深色的背景图片会让昵称不清楚，图片也拉伸变形了...

用一个第三方库裁剪一下了图片，看起来没这么别扭了

![](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-00-728_com.blogofyb.f.png?raw=true)

> > <h4 id="change_user_information">修改用户资料</h4>
> >
> > 用户资料保存之后不会自刷新，这个根据生日计算用的年龄还有问题...
> >
> > ![修改资料](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-06-191_com.blogofyb.f.png?raw=true)
> >
> > <h4 id="change_background">修改空间背景</h4>
> >
> > <h4 id="change_head">修改头像</h4>
> >
> > 从相册里面选择一张照片作为头像或者背景图
> >
> > <h4 id="my_post">我的帖子</h4>
> >
> > 展示自己发表的所有帖子，和查看别人发的帖子的时候一样
> >
> > ![我的帖子](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-11-412_com.blogofyb.f.png?raw=true)
> >
> > <h4 id="count">关注、粉丝、收藏</h4>
> >
> > 展示出自己的关注、粉丝、收藏的数量
> >
> > > > ##### 关注
> > > >
> > > > 所有关注的用户的列表
> > > >
> > > > ##### 粉丝
> > > >
> > > > 所有粉丝的列表
> > > >
> > > > ##### 收藏
> > > >
> > > > 已经收藏的帖子的列表
> >
> > <h4 id="my_response">我的回复</h4>
> >
> > 展示自己所有的回帖记录
> >
> > 点击单个记录可以进入那个帖子
> >
> > ![我的回复](https://github.com/2564800726/Forum/blob/master/image/my_response.png?raw=true)
> >
> > <h4 id="setting">设置</h4>
> >
> > ![设置](https://github.com/2564800726/Forum/blob/master/image/Screenshot_2019-03-01-17-48-17-345_com.blogofyb.f.png?raw=true)
> >
> > > > <h5 id="message_setting">消息设置</h5>
> > > >
> > > > 设置消息的接收
> > > >
> > > > <h5 id="logout">退出登陆</h5>
> > > >
> > > > 清空数据表并返回到[主页](#main)

<h3 id="message">消息</h3>

显示新的消息

<h3 id="#forum_server">ForumServer</h3>

其他的数据交互

[ForumServer](https://github.com/2564800726/ForumServer)

<h3 id="forum_image_server">ForumImageServer</h3>

接收APP上传到服务器的图片

[ForumImageServer](https://github.com/2564800726/ForumImageServer)