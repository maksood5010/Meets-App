package com.meetsportal.meets.utils

import android.graphics.Color
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions

object Constant {


    val meetOption  = BottomSheetOptions.getInstance("Change Name", "Add to calendar", "Make open Meet Up", "Add friend",null,"I’m here","Opt Out","Edit Rule","Cancel Meet Up")

    //var image_uri : Uri? = null


    const val PrivacyUrl = "https://themeetsapp.com/privacy/privacy-policy?no_header=1&no_footer=1"
    const val TermsUrl = "https://themeetsapp.com/terms?no_header=1&no_footer=1"
    val Safety: String = "https://themeetsapp.com/privacy/security-tips"
    val BACK_STACK = "frag"

    val KEY_FOREGROUND_ENABLED = "keyForgroundEnable"


    val TAG_REGISTRATION = "registrationFrament"
    val TAG_NEWS_FEED_FRAGMENT = "newsFeedFragment"
    val TAG_CHAT_DASHBOARD_FRAGMENT = "chatDashboardFragment"
    val TAG_SEARCH_FRAGMENT = "searchFragment"
    val TAG_CREATE_POST_FRAGMENT = "createPostFragment"
    val TAG_SLIDING_IMAGE_FRAGMENT = "slidingImageFragmgent"
    val TAG_CREATE_MEET_UP_FRAGMENT = "createPostFragment"
    val TAG_DETAIL_POST_FRAGMENT = "detailPostFragment"
    val TAG_NOTIFICATION_FRAGMENT = "notificationFragment"
    val TAG_EDIT_POST_IMAGE_FRAGMENT = "editPostImageFragment"
    val TAG_EDIT_POST_FRAGMENT = "editPostFragment"
    val TAG_CROP_IMAGE_FRAGMENT = "cropImageFragment"
    val TAG_MEME_IT_FRAGMENT = "memeItFragment"
    val TAG_MEET_UP_PAGER_FRAGMENT = "MeetUpViewPageFragment"
    val TAG_LIKE_FRAGMENT = "likeFragment"
    val TAG_PROFILE_FRAGMENT = "profileFrament"
    val TAG_FOLLOW_FOLLOWING_FRAGMENT = "followFollowingFragment"
    val TAG_ONE_N_ONE_CHAT = "OneNOneChat"
    val TAG_DM_IMAGE_DETAIL = "DMImageDetailFragment"
    val OTHER_PROFILE_FRAGMENT = "otherProfileFragment"
    val TAG_EXPLORE_FRAGMENT = "exploreFrament"
    val TAG_PLACE_LIST_FRAGMENT = "placeLiostFragment"
    val TAG_PLACE_FILTER_FRAGMENT = "placeFilterFragment"
    val TAG_PLACE_ADD_REVIEW = "placeAddReview"
    val TAG_EDIT_PROFILE = "editProfileFrament"
    val TAG_EDIT_INFO = "editInfoFrament"
    val TAG_FORGET_PASSWORD = "forgetPasswordFrament"
    val TAG_MEET_LIST_FRAGMENT = "MeetUpViewPageFragment"
    val TAG_CAMERA_FRAGMENT = "cameraFrament"
    val TAG_UPLOADVACCINE_FRAGMENT = "uploadVaccine"
    val TAG_MINEMOREMINTS_FRAGMENT = "mineMoreMints"
    val TAG_MYSAFE_FRAGMENT = "mySafe"

    val TAG_SHISHEO_FRAGMENT = "shisheoFrament"
    val TAG_OPEN_FOR_MEETUP_MAP = "openForMeetUpMap"


    val TAG_REASTAURANT_DETAIL = "restaurantDetailFragment"

    //StoragePathrefrence

    //-------------------------------firesbase---------------------------------//
    val POST_NODE = "posts"
    val USERS_NODE = "userProfiles"
    val RESTAURANTS_NODE = "restaurants"
    val POST_IMAGE_FOLDER = "posts";
    val USER_FOLDER = "users"
    val PROFILE_SUBFOLDER = "profile"
    val SPOTLIGHT_SUBFOLDER = "spotlight"

    val NOTIFICATION_NODE = "notifications"
    val NOTIFICATION_TYPES = listOf("like_on_post", "follow_user", "comment_on_post", "reply_on_comment", "like_on_comment", "meetup_invitation","meetup_positive_experience_reminder","meetup_join_requests")
    val DIRECTMESSAGE_NODE = "direct_messaging"
    var MEETUP_CHATS = "meetup_chats"
    val MESSAGE_NODE = "messages"
    val USER_NOTIFICATION_NODE = "userNotifications"
    val STATUS_NODE = "status"


    val URL_EXTRA = "urlExtra"

    //-------------------------------------Sharedprefrence--------------------------//
    val PREFRANCE_OTPRESPONSE = "otpRespaonse"
    val PREFRENCE_PROFILE = "fullProfile"
    val PREFRENCE_LOCATION = "locationprefrence"
    val PREFRENCE_CATEGORY = "placeCategory"
    val PREFRENCE_FACILITY = "placeFacility"
    val PREFRENCE_CUISINE = "placeCuisine"
    val PREFRENCE_INTEREST = "prefrenceFullInterest"
    val PREFERENCE_SHOW_NOTI = "showNotification"
    val PREFERENCE_MSG_NOTI = "showMessageNotification"
    val PREFRENCE_FIRST_TIME = "isUserFirstTimeCame"

    //passing data
    val RESTAURANT_EXTRA = "restaurantExtra"
    val LATITUDE = "latitude"
    val LONGITUDE = "longitude"


    //LRU_Cache
    val NOTIFICATION_LRU = "notificationlru0"


    //----------------------------------REQUEST----------------------------------//
    val CAMERA_REQUEST = 1001
    val GALLERY_IMAGE_REQUEST = 1002
    val GALLERY_VIDEO_REQUEST = 1004
    val AUDIO_REQUEST = 1005
    val GPS_REQUEST = 1052


    //-----------------------------------NotificationType ------------------------//
    val NOTIFICATION_TYPE = "type"
    val ENTITY_ID = "entity_id"
    val PARENT_ID = "parent_id"
    //value

    /*val POST_LIKED_TYPE = "post_liked"
    val FOLLOW_TYPE = "follow_user"
    val POST_COMMENT = "post_comment"
    val COMMENT_LIKED = "comment_liked"
    val REPLY_COMMENT = "reply_comment"
    val MEETUP_INVITATION = "meetup_invitation"*/


    //------------------------

    const val suggested_pref = "suggested_pref"
    const val notification_pref = "notification_pref"
    const val minDist = 100
    const val speed = 120
    const val interval = 10L
    const val  REMAIN_TIME = 60

    val level1 = Badge("bronze", "Bronze", 100, 1, R.drawable.badge_level1, R.drawable.badge_bronze, R.drawable.pin_bronze1, R.color.level1s, R.color.level1e,R.style.bronze)
    val level2 = Badge("silver", "Silver", 200, 2, R.drawable.badge_level2, R.drawable.badge_silver, R.drawable.pin_silver2, R.color.level2s, R.color.level2e,R.style.silver)
    val level3 = Badge("gold", "Gold", 500, 3, R.drawable.badge_level3, R.drawable.badge_gold, R.drawable.pin_gold3, R.color.level3s, R.color.level3e,R.style.gold)
    val level4 = Badge("platinum", "Platinum", 800, 4, R.drawable.badge_level4, R.drawable.badge_platinum, R.drawable.pin_platinum4, R.color.level4s, R.color.level4e,R.style.platinum)
    val level5 = Badge("rhodium", "Rhodium", 1000, 5, R.drawable.badge_level5, R.drawable.badge_rhodium, R.drawable.pin_rhodium5, R.color.level5s, R.color.level5e,R.style.rhodium)
    val level6 = Badge("tanzanite", "Tanzanite", 1200, 6, R.drawable.badge_level6, R.drawable.badge_tanzanite, R.drawable.pin_tanzanite6, R.color.level6s, R.color.level6e,R.style.tanzanite)
    val level7 = Badge("black_opal", "Black Opal", 2500, 7, R.drawable.badge_level7, R.drawable.badge_black_opal, R.drawable.pin_black_opal7, R.color.level7s, R.color.level7e,R.style.black_opal)
    val level8 = Badge("red_beryl", "Red Beryl", 3000, 8, R.drawable.badge_level8, R.drawable.badge_red_beryl, R.drawable.pin_red_beryl8, R.color.level8s, R.color.level8e,R.style.red_beryl)
    val level9 = Badge("musgravite", "Musgravite", 3500, 9, R.drawable.badge_level9, R.drawable.badge_musgravite, R.drawable.pin_musgravite9, R.color.level9s, R.color.level9e,R.style.musgravite)
    val level10 = Badge("alexandrite", "Alexandrite", 5000, 10, R.drawable.badge_level10, R.drawable.badge_alexandrite, R.drawable.pin_alexandrite10, R.color.level10s, R.color.level10e,R.style.alexandrite)
    val level11 = Badge("emerald", "Emerald", 7000, 11, R.drawable.badge_level11, R.drawable.badge_emerald, R.drawable.pin_emerald11, R.color.level11s, R.color.level11e,R.style.emerald)
    val level12 = Badge("ruby", "Ruby", 10000, 12, R.drawable.badge_level12, R.drawable.badge_ruby, R.drawable.pin_ruby12, R.color.level12s, R.color.level12e,R.style.ruby)
    val level13 = Badge("pink_diamond", "Pink Diamond", 12000, 13, R.drawable.badge_level13, R.drawable.badge_pink_diamond, R.drawable.pin_pink_diamond13, R.color.level13s, R.color.level13e,R.style.pink_diamond)
    val level14 = Badge("jadeite", "Jadeite", 15000, 14, R.drawable.badge_level14, R.drawable.badge_jadite, R.drawable.pin_jadeite14, R.color.level14s, R.color.level14e,R.style.jadeite)
    val level15 = Badge("blue_diamond", "Blue Diamond", 20000, 15, R.drawable.badge_level15, R.drawable.badge_blue_diamond, R.drawable.pin_blue_diamond15, R.color.level15s, R.color.level15e,R.style.blue_diamond)

    val badgeList: MutableList<Badge> = mutableListOf(level1, level2, level3, level4, level5, level6, level7, level8, level9, level10, level11, level12, level13, level14, level15)

    enum class JoinTimeType(val type: String){
        TILL_MEETUP("untilMeetUp"),
        TILL_CONFIRM("until_confirm"),
        TIME("time")

    }

    enum class MeetType(val type:String){
        PRIVATE("private"),
        OPEN("open")
    }

    enum class POST_TYPE(val label : Int){
        HEADER(100),
        DEFAULT(101),
        CHECK_IN(102),
        TEXT_POST(103),
        OPEN_MEET(104)
    }



    val MAX_PLACE_SIZE = 5
    enum class PlaceType(val label : String){
        MEET("meets"),
        CUSTOM("custom")

    }

    enum class Post(val type:String){
        DEFAULT("default"),
        CHECK_IN("check_in"),
        TEXT_POST("text_post"),
        OPEN_MEET("open_meetup"),
    }

    enum class RequestType(val value : String){
        PENDING("pending"),
        ACCEPTED("approved"),
        ALL("ALL")
    }
    enum class EnumMeetPerson(val type: String){
        ATTENDEE("attendees"),
        PARTICIPANT("participant"),
        INVITEE("invitee"),
        INTERESTED("interested")
    }

    enum class Source(val sorce:String){
        PROFILE("profile:"),
        PROFILEINSIGHT("profileInsight:"),
        SUGGESTION("suggestions:"),
        LEADERBOARD("leaderboard:"),
        POST("post:"),
        MEETUPCHAT("meetupChat:"),
        NOTIFICATION("notification:".plus(MyApplication.SID)),
        MAP("map:"),
        ONENONECHAT("direct_messaging:"),
        SEARCH("search:")
    }

    enum class VerificationStat(val status:String){
        VERIFIED("passed"),
        NEVER("inactive"),
        PROCESSING("processing"),
        FAILED("failed")
    }

    enum class TextGrad(val label : String, val gradient : IntArray,val textColor : String){
        GRAD1("grad1",intArrayOf(
            Color.parseColor("#8232C9"),
            Color.parseColor("#E200FF")),"#FFFFFF"),
        GRAD2("grad2",intArrayOf(
            Color.parseColor("#2E90CE"),
            Color.parseColor("#1FFF5B")),"#FFFFFF"),
        GRAD3("grad3",intArrayOf(
            Color.parseColor("#CE2E7E"),
            Color.parseColor("#1F28FF")),"#FFFFFF"),
        GRAD4("grad4",intArrayOf(
            Color.parseColor("#C95A32"),
            Color.parseColor("#1FFF41")),"#FFFFFF"),
        GRAD5("grad5",intArrayOf(
            Color.parseColor("#F7E511"),
            Color.parseColor("#E21FFF")),"#FFFFFF"),
        GRAD6("grad6",intArrayOf(
            Color.parseColor("#8A2ECE"),
            Color.parseColor("#00EBFF")),"#FFFFFF"),
        GRAD7("grad7",intArrayOf(
            Color.parseColor("#0009F7"),
            Color.parseColor("#E21FFF")),"#FFFFFF"),
        GRAD8("grad8",intArrayOf(
            Color.parseColor("#32BFC9"),
            Color.parseColor("#1FDDFF")),"#FFFFFF"),
        GRAD9("grad9",intArrayOf(
            Color.parseColor("#CE592E"),
            Color.parseColor("#E21FFF")),"#FFFFFF"),
        GRAD10("grad10",intArrayOf(
            Color.parseColor("#37055C"),
            Color.parseColor("#140317")),"#FFFFFF"),
        GRAD11("grad11",intArrayOf(
            Color.parseColor("#095280"),
            Color.parseColor("#05270E")),"#FFFFFF"),
        GRAD12("grad12",intArrayOf(
            Color.parseColor("#0D0C0C"),
            Color.parseColor("#1F28FF")),"#FFFFFF"),
        GRAD13("grad13",intArrayOf(
            Color.parseColor("#070302"),
            Color.parseColor("#010A02")),"#FFFFFF"),
        GRAD14("grad14",intArrayOf(
            Color.parseColor("#F7E511"),
            Color.parseColor("#1146AC")),"#FFFFFF"),
        GRAD15("grad15",intArrayOf(
            Color.parseColor("#8A2ECE"),
            Color.parseColor("#AD3002")),"#FFFFFF"),
        GRAD16("grad16",intArrayOf(
            Color.parseColor("#020314"),
            Color.parseColor("#166F0A")),"#FFFFFF"),
        GRAD17("grad17",intArrayOf(
            Color.parseColor("#000000"),
            Color.parseColor("#00D8FF")),"#FFFFFF"),
        GRAD18("grad18",intArrayOf(
            Color.parseColor("#FFA7A7"),
            Color.parseColor("#E21FFF")),"#FFFFFF"),
        GRAD19("grad19",intArrayOf(
            Color.parseColor("#9300FF"),
            Color.parseColor("#140317")),"#FFFFFF"),
        GRAD20("grad20",intArrayOf(
            Color.parseColor("#095280"),
            Color.parseColor("#E2FF00")),"#FFFFFF"),
        GRAD21("grad21",intArrayOf(
            Color.parseColor("#0D0C0C"),
            Color.parseColor("#FFB11F")),"#FFFFFF"),
        GRAD22("grad22",intArrayOf(
            Color.parseColor("#070302"),
            Color.parseColor("#0574C6")),"#FFFFFF"),
        GRAD23("grad23",intArrayOf(
            Color.parseColor("#F7E511"),
            Color.parseColor("#29AC11")),"#FFFFFF"),
        GRAD24("grad24",intArrayOf(
            Color.parseColor("#8A2ECE"),
            Color.parseColor("#02AD09")),"#FFFFFF"),
        GRAD25("grad25",intArrayOf(
            Color.parseColor("#020314"),
            Color.parseColor("#24CE0D")),"#FFFFFF"),
        GRAD26("grad26",intArrayOf(
            Color.parseColor("#000000"),
            Color.parseColor("#F40B0B")),"#FFFFFF"),
        GRAD27("grad27",intArrayOf(
            Color.parseColor("#FFA7A7"),
            Color.parseColor("#C9A632")),"#FFFFFF"),
        GRAD28("grad28",intArrayOf(
            Color.parseColor("#EEEEEE"),
            Color.parseColor("#C4C4C4")),"#000000"),


    }

    val GradientTypeArray = arrayOf(
        TextGrad.GRAD28,
        TextGrad.GRAD1, TextGrad.GRAD2, TextGrad.GRAD3,
        TextGrad.GRAD4, TextGrad.GRAD5, TextGrad.GRAD6,
        TextGrad.GRAD7, TextGrad.GRAD8, TextGrad.GRAD9,
        TextGrad.GRAD10, TextGrad.GRAD11,TextGrad.GRAD12,
        TextGrad.GRAD13, TextGrad.GRAD14,TextGrad.GRAD15,
        TextGrad.GRAD16, TextGrad.GRAD17,TextGrad.GRAD18,
        TextGrad.GRAD19, TextGrad.GRAD20,TextGrad.GRAD21,
        TextGrad.GRAD22, TextGrad.GRAD23,TextGrad.GRAD24,
        TextGrad.GRAD25, TextGrad.GRAD26,TextGrad.GRAD27
    )


    //*************************** Custom Error Code **********************//
    val NO_INTERNET = 1500
    val CODE_NO_INTERNET="custom.error.NoInternet"
    val MESSAGE_NO_INTERNET="No Internet Connection!!"



    //**********************************MIX PANEL TRACK****************************************

    //*****************Viewed********************

    const val VwFeed_Global =  "global_feed_screen_viewed"
    const val VwFeed_TimeLine =  "timeline_feed_screen_viewed"
    const val VwOpenMeetDiscover = "open_meet_up_list_screen_viewed_by_filter"
    const val VwSuggestedPage = "suggested_list_screen_viewed"
    const val VwTextPostViewed = "text_post_detail_screen_viewed"
    const val VwImagePostViewed = "image_post_detail_screen_viewed"
    const val VwOPMeetPostViewed = "open_meetup_post_detail_screen_viewed"
    const val VwMapViewed = "map_screen_viewed"
    const val VwNotiViewed = "notification_screen_viewed"
    const val VwChatDashViewed = "chat_list_screen_viewed"
    const val VwSearchPeopleViewed = "search_people_screen_viewed"
    const val VwSearchPostViewed = "search_post_screen_viewed"
    const val VwSearchPlaceViewed = "search_places_screen_viewed"
    const val VwPostInsightViewed = "post_insight_screen_viewed"

    const val VwMeetUpTab = "meet_up_tab_home_screen_viewed"
    const val VwUpComingMeetList = "my_private_meet_up_list_screen_viewed"
    const val VwPreviousMeetList = "my_previous_meet_up_list_viewed"
    const val VwOpenMeetList = "my_open_meet_up_list_screen_viewed"
    const val VwInviteOpenMeetScreen = "invites_to_open_meet_up_list_screen_viewed"

    const val VwPlaceTab = "places_home_tab_screen_viewed"
    const val VwBestPlaceList = "best_places_screen_viewed"
    const val VwSeeAllPlaceList = "see_all_places_list_viewed"
    const val VwCategoryPlaceList = "categories_screen_viewed"

    const val VwMyProfileView = "view_my_profile_screen"
    const val VwOtherProfileView = "view_other_user_profile_screen"
    const val VwLeaderBoardView = "view_leaderboard_screen"
    const val VwMySafeView = "view_my_safe_screen"

    //*****************Actions********************

    const val AcHomePageDp = "clicked_my_profile_photo_on_home"
    const val AcCreatMeetHome = "clicked_create_meet_up_button"
    const val AcCreatePostFrHome = "clicked_on_create_post"
    const val AcSeeAllSuggestFrHome = "clicked_on_see_all_suggestions"
    const val AcDpSuggested = "suggested_user_profile_clicked"
    const val AcFeedChange = "clicked_on_feeds_filter"
    const val AcGlobalFeed ="clicked_on_global_feeds_filter"
    const val AcTimeLineFeed ="clicked_on_timeline_feeds_filter"
    const val AcOpenMeetFeed ="clicked_on_open_meetup_feeds_filter"
    const val AcDpOnTimeLine = "clicked_on_user_profile_on_timeline"
    const val AcEditImagePost = "clicked_on_edit_post"
    const val AcDisableCmnt = "clicked_on_disable_comment"
    const val AcDeletePost = "click_on_delete_post"
    const val AcPostImgToDetail = "click_on_post_image_for_detail_page"
    const val AcPostCmtToDetail = "clicked_on_comment_button_for_detail_page"
    const val AcPostlikeButton = "clicked_on_like_button_under_comment"
    const val AcPostLikeCount = "clicked_on_likes_count"
    const val AcPostCmtCount = "clicked_on_comments_count"
    const val AcPostInsight = "clicked_on_insight"
    const val AcMapOnHome = "clicked_on_map_icon_from_home"
    const val AcNotiOnHome = "clicked_on_notification_from_home"
    const val AcNotiText = "clicked_on_notification_text_to_view"
    const val AcDmOnHome = "clicked_dm_from_home"
    const val AcFollowOnSuggestion = "clicked_on_follow_on_suggested_home_screen"
    const val AcMentionOnHome = "clicked_on_mention_from_home"

    const val AcProfileVisitNoti = "clicked_on_user_visited_your_profile_button"
    const val AcDpOnProfileView = "clicked_on_user_profile_in_list_to_view_user"
    const val AcFollowUnFollowProfileView = "clicked_on_follow_unflow_button"
    const val AcSeeThemProfileView = "clicked_on_see_them"
    const val AcUnlockProfileView = "clicked_on_unlock button"
    const val AcJoinOpMTimeline = "clicked_join_open_meet_up_button"
    const val AcCardtoDetailOpMeet = "clicked_open_meet_up_card_to_detail"
    const val AcOpMeetInterested = "clicked_on_interested_people"
    const val AcOpMeetView = "clicked_on_view_button"
    const val AcOpMeetLocation = "clicked_on_location_text_on_open_meetup_in_timeline"
    const val AcCoverImage = "clicked_on_cover_image"
    const val AcEditCoverImage = "clicked_on_edit_text"
    const val AcCoverTakePhoto = "clicked_on_take_photo_text_to_change_cover_photo"
    const val AcCoverChoosePhoto = "clicked_on_choose_photo_text_to_change_cover_photo"
    const val AcCoverCancelPhoto = "clicked_on_cancel_to_quite_cover_edit"
    const val AcProfileImage = "clicked_on_profile_image_on_profile_screen"
    const val AcVerifiedIcon = "clicked_on_verified_icon"
    const val AcWorthIcon = "clicked_on_worth_button"
    const val AcMoreIcon = "clicked_on_more_icon"
    const val AcPostCount = "clicked_on_post_count"
    const val AcFollowerCount = "clicked_on_followers_count"
    const val AcFollowingCount = "clicked_on_following_count"
    const val AcMeetupProfile = "clicked_on_meet_up_button_on_profile_screen"
    const val AcCreatePostProfile = "clicked_make_a_post_on_profile_screen"
    const val AcViewedMeIcon = "clicked_on_viewed_my_profile_on_profile_screen"
    const val AcMySafeIcon = "clicked_on_safe_from_profile_screen"
    const val AcEditInterest = "clicked_on_interest_button_on_user_profile"
    const val AcMeetsCredibility = "clicked_on_meets_credibility_badge_on_profile"
    const val AcMineMoreMints = "clicked_on_mine_more_mints_from_popup_screen"
    const val AcOMDetailLocation = "clicked_on_location_text_on_open_meetup_detail_screen"
    const val AcOMDetailIamHere = "clicked_on_i_am_here_button_on_open_meetup_detail_screen"
    const val AcProfilePositiveExp = "clicked_on_positive_exp_button_in_profile_screen"
    const val AcProfileMeetCount = "clicked_on_meet_up_count_button"
    const val AcProfileGridTab = "clicked_on_posts_tab_for_grid_view_on_profile"
    const val AcProfileListTab = "clicked_on_posts_tab_for_list_view_on_profile"
    const val AcProfileGridView = "clicked_on_post_in_grid_view"
    const val AcProfileDotView = "clicked_on_three_dot_icon_for_more_option_on_post_in_list_view_on_profile_screen"
    const val AcProfilePostLongPress = "longpressed_on_post_for_more_option_on_post_in_list_view_on_profile_screen"
    const val AcProfileOMDetail = "clicked_on_open_meet_up_card_to_detail_from_own_profile"
    const val AcProfileOMInterested = "clicked_on_interested_people_from_own_profile"
    const val AcProfileCheckInName = "clicked_on_checked_in_place_name_from_own_profile"
    const val AcMeetupEnterChat = "clicked_on_meet_up_card_to_enter_chat"
    const val AcMeetupListEnterChat = "clciked_on_meet_up_card_on_up_coming_meet_up_screen_to_enter_chat"
    const val AcMeetupListInterested = "clicked_on_interested_users_on_up_coming_meet_up_screen"
    const val AcMeetupListFilter = "clicked_on_filter_icon_on_up_coming_meet_up_screen"
    const val AcPMViewAll = "clicked_on_view_all_up_coming_meet_ups_on_meet_up_tab"
    const val AcPreviousMeetViewAll = "clicked_on_view_all_up_coming_meet_ups_on_meet_up_tab"
    const val AcPMInterestedList = "clicked_on_interested_users_private_meet_up_card_in_meet_up_tab"
    const val AcOMInterestedList = "clicked_on_interested_users_open_meet_up_card_in_meet_up_tab"
    const val AcOMViewDetail = "clicked_on_interested_users_open_meet_up_card_in_meet_up_tab"
    const val AcOMViewAll = "clicked_on_view_all_button_for_open_meet_up_in_meet_up_tab"
    const val AcPreviousDetailChat = "clciked_on_previous_meet_up_card_to_enter_chat"
    const val AcMeetUpCreateOne = "clciked_on_create_one_button_on_up_coming_meet_up_screen_when_list_is_empty"
    const val AcMeetUpVotedPlace = "clicked_on_main_place_display_card_with_place_name_and_rating"
    const val AcMeetUpConfirm = "clicked_on_confirm_button_in_meet_up_chat"
    const val AcMeetUpPieChart = "clicked_on_pie_chat_ring_to_view_vote_detail"
    const val AcMeetUpChatParticipant = "clicked_on_participant_profile_photo_in_meet_up_chat_panel_header"
    const val AcMeetUpChatName = "clicked_on_meet_up_title_on_chat_panel"
    const val AcMeetUpChatMore = "clicked_on_more_options_button_on_chat_panel_title"
    const val AcMeetUpChatUserName = "clicked_username_in_vote_chat_to_view_user_profile"
    const val AcMeetUpChatPlaceName = "clicked_on_place_name_in_vote_chat_to_preview_place"
    const val AcMeetUpChatChosenPlace = "clicked_on_places_vote_card_at_bottom_of_chat"
    const val AcMeetUpChatAddPlace = "clicked_on_add_place_plus_button_in_meet_up_chat"
    const val AcMeetUpChatSend = "clicked_on_send_message_button_in_meet_up_chat"
    const val AcPlacesTab = "clicked_on_places_tab"
    const val AcPlacesDenyLocation = "clicked_on_allow_location_no_button"
    const val AcPlacesAllowLocation = "clicked_on_allow_location_yes_button"
    const val AcPlacesCategories = "clicked_on_categories_button_to_view_places_categories"
    const val AcPlacesBestJoints = "clicked_on_best_meet_up_joints_card"
    const val AcPlacesBestJointsSeeAll = "clicked_on_see_all_text_for_best_meet_up_joints"
    const val AcPlacesCarousal = "clicked_on_place_carousel_ad"
    const val AcPlacesCloseBy = "clicked_on_places_close_by_card"
    const val AcPlacesCloseBySeeAll = "clicked_on_see_all_text_for_places_close_by"
    const val AcPlacesSavedFloating = "clicked_on_saved_floating_button"
    const val AcMapPlacesTab = "clicked_on_places_tab_on_map"
    const val AcMapPeopleTab = "clicked_on_people_tab_on_map"
    const val AcMapPeopleCluster = "clicked_on_user_profile_pin_icon"
    const val AcMapPlaceCluster = "clicked_on_cluster_icon"
    const val AcMapSearchArea = "clicked_on_search_this_aread_button"
    const val AcMapInterest = "clicked_interests_icon"
    const val AcMapCategories = "clicked_categories_icon"
    const val AcMapProfileImage = "clicked_on_user_profile_image_on_profile_preview_on_map"
    const val AcMapProfileUserName = "clicked_on_user_profile_username_on_profile_preview_on_map"
    const val AcMapProfileMeetup = "clicked_meet_up_button_on_profile_preview_on_map"
    const val AcMapProfileChat = "clicked_on_chat_button_on_profile_preview_on_map"
    const val AcMapProfileFollow = "clicked_on_follow_button_on_profile_preview_on_map"
    const val AcMapProfileBadge = "clicked_on_badge_card_on_profile_preview_on_map"
    const val AcMapProfilePositiveExp = "clicked_on_positive_experience_card_on_profile_preview_on_map"
    const val AcMapProfileMeetCount = "clicked_on_meet_up_count_card_on_profile_preview_on_map"
    const val AcMapProfileUserMap = "clicked_on_map_canvas_on_profile_preview_on_map"
    const val AcMapProfileInviteMeet = "clicked_on_invite_to_meet_up_button_on_profile_preview_on_map"
    const val AcOMDetailProfile = "clicked_on_header_profile_picture_on_open_meetup_detail_screen"
    const val AcPostDelete = "click_on_delete_post"
    const val AcCreatePrivateMeet = "clicked_on_create_button_for_private_meetup"
    const val AcSelectUsername = "clicked_on_user_name_to_select_user"
    const val AcSearchMeetUser = "clicked_in_search_bar_in_friends_list_screen"
    const val AcNextFriends = "clicked_next_button_on_friends_list_screen"
    const val AcNextFriendBack = "clicked_on_back_button_friends_list_screen"
    const val AcPlaceSearchBar = "clicked_in_search_bar_in_places_list_screen"
    const val AcSlideCategories = "slide_places_categories"
    const val AcTrendingPlaces = "clicked_on_trending_places_category_button"
    const val AcCustomPlaces = "clicked_on_custom_place_button"
    const val AcSavedPlaces = "clicked_on_saved_places_button"
    const val AcCreateMeetPlaceImage = "clicked_on_place_card_image_in_list_of_places_on_places_screen"
    const val AcCreateMeetAddPlace = "clicked_on_add_place_button_on_places_screen"
    const val AcCreateMeetSelectedPlace = "clicked_on_add_place_button_on_selected_places_screen"
    const val AcCreateMeetSelectedPlaceBack = "clicked_back_button_on_selected_places_screen"
    const val AcCreateMeetRuleAdd = "clicked_on_next_button_on_meetup_rules_screen"
    const val AcCreateMeetupInvitation = "clicked_on_send_invitation_on_review_meetu_screen"
    const val AcCreateMeetupReviewPlace = "clicked_on_selected_place_on_review_meetup_screen"
    const val AcCreateMeetupReviewPlaceRemove = "clicked_remove_icon_on_selected_user_on_reveiw_meetup_screen"
    const val AcCreateMeetupEnterChat = "clicked_enter_chat_on_invitation_sent_pop_up_screen"


}