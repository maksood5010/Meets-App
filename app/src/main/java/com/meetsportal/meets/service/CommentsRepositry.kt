package com.meetsportal.meets.service

import com.meetsportal.meets.models.Comment
import com.meetsportal.meets.models.UserProfile
import java.util.*

class CommentsRepositry {

    companion object {
        var commentList = arrayOf(
            Comment(
                id =
                "1",
                user = UserProfile(
                    id = "1",
                    firstName = "Khan",
                    lastName = "Imran",
                    displayName = "Imran",
                    photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                    verified = true,
                    followers = 13
                ),
                caption = "Hello",
                time = Date(),
                type = "user",
                replies = arrayOf(
                    Comment(
                        id = "1",
                        user = UserProfile(
                            id = "1",
                            firstName = "111",
                            lastName = "Imran",
                            displayName = "Imran",
                            photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                            verified = true,
                            followers = 13
                        ),
                        caption = "Hello",
                        time = Date(),
                        type = "user",
                        replies = null,
                        likeCount = 2,
                        commentCount = 3
                    ),
                    Comment(
                        id = "1",
                        user = UserProfile(
                            id = "1",
                            firstName = "111",
                            lastName = "Imran",
                            displayName = "Imran",
                            photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                            verified = true,
                            followers = 13
                        ),
                        caption = "Hello",
                        time = Date(),
                        type = "user",
                        replies = null,
                        likeCount = 2,
                        commentCount = 3
                    )
                ).toList(),
                likeCount = 2,
                commentCount = 5
            ),
            Comment(
                id =
                "1",
                user = UserProfile(
                    id = "1",
                    firstName = "Khan",
                    lastName = "Imran",
                    displayName = "Imran",
                    photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                    verified = true,
                    followers = 13
                ),
                caption = "Hello",
                time = Date(),
                type = "user",
                replies = arrayOf(
                    Comment(
                        id = "1",
                        user = UserProfile(
                            id = "1",
                            firstName = "111",
                            lastName = "Imran",
                            displayName = "Imran",
                            photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                            verified = true,
                            followers = 13
                        ),
                        caption = "Hello",
                        time = Date(),
                        type = "user",
                        replies = null,
                        likeCount = 2,
                        commentCount = 3
                    ),
                    Comment(
                        id = "1",
                        user = UserProfile(
                            id = "1",
                            firstName = "111",
                            lastName = "Imran",
                            displayName = "Imran",
                            photoUrl = "https://images.pexels.com/photos/358457/pexels-photo-358457.jpeg",
                            verified = true,
                            followers = 13
                        ),
                        caption = "Hello",
                        time = Date(),
                        type = "user",
                        replies = null,
                        likeCount = 2,
                        commentCount = 3
                    )
                ).toList(),
                likeCount = 2,
                commentCount = 5
            )
        )
    }

}