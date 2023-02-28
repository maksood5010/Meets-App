package com.meetsportal.meets.service

import com.google.firebase.Timestamp
import com.meetsportal.meets.models.Comment
import com.meetsportal.meets.models.Post
import com.meetsportal.meets.models.UserProfile

class PostDetailRepositry {

    companion object {
        var postDetails = arrayOf(
            Post(
                id = "1",
                user = UserProfile(
                    id = "1",
                    firstName = "Abu",
                    lastName = "Asad",
                    displayName = "Asad",
                    photoUrl = "https=//images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                    verified = true,
                    followers = 13
                ),
                createdOn = Timestamp.now(),
                type = "user",
                images = arrayOf(mapOf("path" to "https=//images.pexels.com/photos/3116416/pexels-photo-3116416.jpeg"), mapOf("path" to "https=//images.pexels.com/photos/1591382/pexels-photo-1591382.jpeg")).toList(),
                mediaType = "video",
                caption = "Hello",
                comments = arrayOf(Comment()).toList(),
                likeCount = 3,
                commentCount = 6
            )
        )
    }
}