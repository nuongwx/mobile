# Datto
Datto là sự kết hợp giữa Dating và Together. Ứng dụng này được tạo ra để các nhóm bạn hoặc cặp đôi quản lý các hoạt động chung của họ, bao gồm đưa ra thông báo về những ngày đáng nhớ và lên lịch cho một dịp đi chơi cùng nhau.

## Folder structure

```bash
mobile
├── .github
│   └── workflows
│       ├── CD.yml
│       └── validate.yml
├── .gitignore
├── .gradle
├── .idea
├── app
│   ├── .gitignore
│   ├── build
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   └── src
│       ├── androidTest
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── ic_launcher-playstore.png
│       │   ├── java
│       │   │   └── com
│       │   │       └── example
│       │   │           └── datto
│       │   │               ├── API
│       │   │               │   ├── APICallback.kt
│       │   │               │   ├── APIInterface.kt
│       │   │               │   ├── APIService.kt
│       │   │               │   └── RetrofitInstance.kt
│       │   │               ├── AppLayout.kt
│       │   │               ├── ChangePassword.kt
│       │   │               ├── Create.kt
│       │   │               ├── Credential
│       │   │               │   └── CredentialService.kt
│       │   │               ├── DataClass
│       │   │               │   ├── Account.kt
│       │   │               │   ├── BaseResponse.kt
│       │   │               │   ├── BucketResponse.kt
│       │   │               │   ├── ChangePasswordRequest.kt
│       │   │               │   ├── Credential.kt
│       │   │               │   ├── EventMemberResponse.kt
│       │   │               │   ├── EventResponse.kt
│       │   │               │   ├── Fund.kt
│       │   │               │   ├── Group.kt
│       │   │               │   ├── InviteCodeResponse.kt
│       │   │               │   ├── JoinGroupRequest.kt
│       │   │               │   ├── Memory.kt
│       │   │               │   ├── NewGroup.kt
│       │   │               │   ├── NotificationRequest.kt
│       │   │               │   ├── Otp.kt
│       │   │               │   ├── Planning.kt
│       │   │               │   ├── Profile.kt
│       │   │               │   └── SplitFundResponse.kt
│       │   │               ├── EnterNewPasswordActivity.kt
│       │   │               ├── EventAdapter.kt
│       │   │               ├── EventDetails.kt
│       │   │               ├── EventWidget.kt
│       │   │               ├── FundEdit.kt
│       │   │               ├── FundList.kt
│       │   │               ├── GlobalVariable
│       │   │               │   └── GlobalVariable.kt
│       │   │               ├── GroupDetails.kt
│       │   │               ├── GroupDetailsEventList.kt
│       │   │               ├── GroupDetailsMemberList.kt
│       │   │               ├── GroupEdit.kt
│       │   │               ├── GroupList.kt
│       │   │               ├── JoinGroup.kt
│       │   │               ├── MainActivity.kt
│       │   │               ├── Memories.kt
│       │   │               ├── MemoryView.kt
│       │   │               ├── MyItemRecyclerViewAdapter.kt
│       │   │               ├── NewFund.kt
│       │   │               ├── NewGroup.kt
│       │   │               ├── NewGroupInviteCode.kt
│       │   │               ├── NewMemory.kt
│       │   │               ├── Notification.kt
│       │   │               ├── placeholder
│       │   │               │   └── PlaceholderContent.kt
│       │   │               ├── PlanningEdit.kt
│       │   │               ├── PlanningList.kt
│       │   │               ├── Profile.kt
│       │   │               ├── ProfileEdit.kt
│       │   │               ├── ResetPasswordActivity.kt
│       │   │               ├── SignInActivity.kt
│       │   │               ├── SignUpActivity.kt
│       │   │               ├── SignUpInfoActivity.kt
│       │   │               ├── utils
│       │   │               │   ├── FirebaseNotification.kt
│       │   │               │   ├── FirebaseNotificationService.kt
│       │   │               │   ├── GoogleAuth.kt
│       │   │               │   ├── NumberTextWatcher.kt
│       │   │               │   └── WidgetUpdater.kt
│       │   │               ├── VerifyOtpActivity.kt
│       │   │               └── W2M.kt
│       │   └── res
│       │       ├── drawable
│       │       ├── ....
│       │       ├── font
│       │       │   ├── be_vietnam.xml
│       │       │   ├── be_vietnam_bold.xml
│       │       │   ├── be_vietnam_bold_italic.xml
│       │       │   ├── be_vietnam_extrabold.xml
│       │       │   ├── be_vietnam_extrabold_italic.xml
│       │       │   ├── be_vietnam_italic.xml
│       │       │   ├── be_vietnam_light.xml
│       │       │   ├── be_vietnam_light_italic.xml
│       │       │   ├── be_vietnam_medium.xml
│       │       │   ├── be_vietnam_medium_italic.xml
│       │       │   ├── be_vietnam_pro.xml
│       │       │   ├── be_vietnam_semibold.xml
│       │       │   ├── be_vietnam_semibold_italic.xml
│       │       │   ├── be_vietnam_thin.xml
│       │       │   └── be_vietnam_thin_italic.xml
│       │       ├── layout
│       │       │   ├── activity_app_layout.xml
│       │       │   ├── activity_enter_new_password.xml
│       │       │   ├── activity_main.xml
│       │       │   ├── activity_reset_password.xml
│       │       │   ├── activity_sign_in.xml
│       │       │   ├── activity_sign_up.xml
│       │       │   ├── activity_sign_up_info.xml
│       │       │   ├── activity_verify_otp.xml
│       │       │   ├── calendar_day_legend_container.xml
│       │       │   ├── calendar_day_legend_text.xml
│       │       │   ├── dialog_split_funds.xml
│       │       │   ├── events_list_items.xml
│       │       │   ├── event_details_planning_daily_items.xml
│       │       │   ├── event_details_planning_date_title.xml
│       │       │   ├── event_details_planning_list_items.xml
│       │       │   ├── event_widget.xml
│       │       │   ├── expense_card.xml
│       │       │   ├── fragment_change_password.xml
│       │       │   ├── fragment_create.xml
│       │       │   ├── fragment_event_details.xml
│       │       │   ├── fragment_fund_edit.xml
│       │       │   ├── fragment_fund_list.xml
│       │       │   ├── fragment_group_details.xml
│       │       │   ├── fragment_group_details_event_list.xml
│       │       │   ├── fragment_group_details_member_list.xml
│       │       │   ├── fragment_group_edit.xml
│       │       │   ├── fragment_group_list.xml
│       │       │   ├── fragment_join_group.xml
│       │       │   ├── fragment_memories.xml
│       │       │   ├── fragment_memory_view.xml
│       │       │   ├── fragment_new_fund.xml
│       │       │   ├── fragment_new_group.xml
│       │       │   ├── fragment_new_group_invite_code.xml
│       │       │   ├── fragment_new_memory.xml
│       │       │   ├── fragment_notification.xml
│       │       │   ├── fragment_notification_list.xml
│       │       │   ├── fragment_planning_edit.xml
│       │       │   ├── fragment_planning_list.xml
│       │       │   ├── fragment_profile.xml
│       │       │   ├── fragment_profile_edit.xml
│       │       │   ├── fragment_w2m.xml
│       │       │   ├── groups_list_items.xml
│       │       │   ├── group_details_events_list_items.xml
│       │       │   ├── group_details_members_list_items.xml
│       │       │   ├── group_details_memories_cover_items.xml
│       │       │   ├── split_fund_item.xml
│       │       │   ├── w2m_calendar_day.xml
│       │       │   └── w2m_calendar_header.xml
│       │       ├── menu
│       │       │   ├── bottom_app_bar_menu.xml
│       │       │   └── top_app_bar_menu.xml
│       │       ├── ...
│       │       ├── values
│       │       │   ├── attrs.xml
│       │       │   ├── colors.xml
│       │       │   ├── dimens.xml
│       │       │   ├── font_certs.xml
│       │       │   ├── preloaded_fonts.xml
│       │       │   ├── strings.xml
│       │       │   ├── styles.xml
│       │       │   ├── themes.xml
│       │       │   └── theme_overlays.xml
│       │       ├── values-night
│       │       │   ├── colors.xml
│       │       │   ├── themes.xml
│       │       │   └── theme_overlays.xml
│       │       ├── values-night-v31
│       │       │   └── themes.xml
│       │       ├── values-v21
│       │       │   └── styles.xml
│       │       ├── values-v31
│       │       │   ├── styles.xml
│       │       │   └── themes.xml
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           ├── data_extraction_rules.xml
│       │           ├── event_widget_info.xml
│       │           └── network_security_config.xml
│       └── test
├── build.gradle.kts
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── README.md
└── settings.gradle.kts
```

## Backend
Yêu cầu môi trường:
- Backend: Express.JS, MongoDB, Firebase
- [Repo backend](https://github.com/ducviet5138/crab-backend)
- Node.JS: 21.6.2

Các cài đặt cần thiết cho môi trường:
- Tạo database ở MongoDB có tên: datto
- Thêm các thông tin cần thiết cho file .env:
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=<email>
MAIL_PASS=<app password>
JWT_SECRET=<secret>
```

Thêm `Account Service (.json)` lấy từ firebase. Setup firebase cho authentication.

Sau khi thực hiện xong các bước trên thì có thể chạy server backend bằng lệnh: 
```bash
npm start
```
## Ứng dụng
- Phiên bản Android: từ 13 trở lên.
- Cập nhật fingerprint ứng dụng & lưu file thông tin `google-services.json` lấy từ Firebase.
- Cập nhật biến `GlobalVariable.BASE_URL` tương ứng với thông tin địa chỉ host API của server backend.
  
*Lưu ý: Trong trường hợp server EC2 còn chạy thì có thể không phải đổi `GlobalVariable.BASE_URL`.*
