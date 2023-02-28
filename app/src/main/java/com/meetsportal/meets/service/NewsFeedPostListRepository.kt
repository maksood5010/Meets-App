package com.meetsportal.meets.service

class NewsFeedPostListRepository {

    companion object {
        /*var newsFeedPostLists = arrayOf(
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran1",
                displayName = "Imran1",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://moodle.htwchur.ch/pluginfile.php/124614/mod_page/content/4/example.jpg","https://i.pinimg.com/videos/thumbnails/originals/dd/24/bb/dd24bb9cd68e9e25d1def88cad0a9ea7-00001.jpg","https://i.pinimg.com/videos/thumbnails/originals/0d/29/18/0d2918323789eabdd7a12cdd658eda04-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ba/a3/07/baa307a7de3030f0073c56fa95ab2a3c.jpg", "https://v.pinimg.com/videos/720p/77/4f/21/774f219598dde62c33389469f5c1b5d1.mp4").toList(),
            mediaType = "video",
            caption = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran2",
                displayName = "Imran2",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/dd/24/bb/dd24bb9cd68e9e25d1def88cad0a9ea7-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ba/a3/07/baa307a7de3030f0073c56fa95ab2a3c.jpg", "https://v.pinimg.com/videos/720p/75/40/9a/75409a62e9fb61a10b706d8f0c94de9a.mp4").toList(),
            mediaType = "video",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran3",
                displayName = "Imran3",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/dd/24/bb/dd24bb9cd68e9e25d1def88cad0a9ea7-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ba/a3/07/baa307a7de3030f0073c56fa95ab2a3c.jpg", "https://v.pinimg.com/videos/720p/65/b0/54/65b05496c385c89f79635738adc3b15d.mp4").toList(),
            mediaType = "video",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran4",
                displayName = "Imran4",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/0d/29/18/0d2918323789eabdd7a12cdd658eda04-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ef/17/51/ef17519f5e473adc01dfd64c35cf44d4.jpg").toList(),
            mediaType = "image",
            caption = " It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran5",
                displayName = "Imran5",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/dd/24/bb/dd24bb9cd68e9e25d1def88cad0a9ea7-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ba/a3/07/baa307a7de3030f0073c56fa95ab2a3c.jpg", "https://v.pinimg.com/videos/720p/77/4f/21/774f219598dde62c33389469f5c1b5d1.mp4").toList(),
            mediaType = "video",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran6",
                displayName = "Imran6",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/d5/15/78/d51578c69d36c93c6e20144e9f887c73-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/97/a5/51/97a5513d3c512eb382e564ba542d917b.jpg").toList(),
            mediaType = "image",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran7",
                displayName = "Imran7",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/c2/6d/2b/c26d2bacb4a9f6402d2aa0721193e06e-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/ef/17/51/ef17519f5e473adc01dfd64c35cf44d4.jpg", "https://v.pinimg.com/videos/720p/75/40/9a/75409a62e9fb61a10b706d8f0c94de9a.mp4").toList(),
            mediaType = "video",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ), Post(id = "1", user = UserProfile(id = "1", firstName = "Khan", lastName = "Imran", displayName = "Imran", photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg", verified = true, followers = 13), createdOn = Timestamp.now(), type = "user", images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/62/81/60/628160e025f9d61b826ecc921b9132cd-00001.jpg").toList(), videos = arrayOf("https://i.pinimg.com/564x/72/c1/a8/72c1a8aabbfe782643c4a5e739ec0ed2.jpg").toList(), mediaType = "image", caption = "Hello", comments = arrayOf(), likeCount = 3, commentCount = 6),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran8",
                displayName = "Imran8",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/5f/aa/3d/5faa3d057eb31dd05876f622ea2e7502-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/6f/5f/fb/6f5ffb82a1f9a9f7e478b8a2486831f5.jpg", "https://v.pinimg.com/videos/720p/0d/29/18/0d2918323789eabdd7a12cdd658eda04.mp4").toList(),
            mediaType = "video",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran9",
                displayName = "Imran9",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/65/b0/54/65b05496c385c89f79635738adc3b15d-00001.jpg").toList(),
            videos = arrayOf("https://v.pinimg.com/videos/720p/65/b0/54/65b05496c385c89f79635738adc3b15d.mp4").toList(),
            mediaType = "image",
            caption = """
'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s'
""",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran10",
                displayName = "Imran10",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/86/a1/c6/86a1c63fc58b2e1ef18878b7428912dc-00001.jpg").toList(),
            videos = arrayOf("https://v.pinimg.com/videos/720p/77/4f/21/774f219598dde62c33389469f5c1b5d1.mp4").toList(),
            mediaType = "image",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ), Post(id = "1", user = UserProfile(id = "1", firstName = "Khan", lastName = "Imran", displayName = "Imran", photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg", verified = true, followers = 13), createdOn = Timestamp.now(), type = "user", images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/77/4f/21/774f219598dde62c33389469f5c1b5d1-00001.jpg").toList(), videos = arrayOf("https://i.pinimg.com/564x/97/a5/51/97a5513d3c512eb382e564ba542d917b.jpg", "https://v.pinimg.com/videos/720p/dd/24/bb/dd24bb9cd68e9e25d1def88cad0a9ea7.mp4").toList(), mediaType = "video", caption = "Hello", comments = arrayOf(), likeCount = 3, commentCount = 6),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran11",
                displayName = "Imran11",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/77/4f/21/774f219598dde62c33389469f5c1b5d1-00001.jpg").toList(),
            videos = arrayOf("https://v.pinimg.com/videos/720p/77/4f/21/774f219598dde62c33389469f5c1b5d1.mp4").toList(),
            mediaType = "image",
            caption = "Hello",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        ),
        Post(
            id = "1",
            user = UserProfile(
                id = "1",
                firstName = "Khan",
                lastName = "Imran12",
                displayName = "Imran12",
                photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                verified = true,
                followers = 13
            ),
            createdOn = Timestamp.now(),
            type = "user",
            images = arrayOf("https://i.pinimg.com/videos/thumbnails/originals/77/4f/21/774f219598dde62c33389469f5c1b5d1-00001.jpg").toList(),
            videos = arrayOf("https://i.pinimg.com/564x/72/c1/a8/72c1a8aabbfe782643c4a5e739ec0ed2.jpg", "https://v.pinimg.com/videos/720p/d5/15/78/d51578c69d36c93c6e20144e9f887c73.mp4").toList(),
            mediaType = "video",
            caption = """
'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s'
""",
            comments = arrayOf(),
            likeCount = 3,
            commentCount = 6
        )
            )*/
    }


}