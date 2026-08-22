# Canvas LMS API 与路由映射

## routes.rb 路由声明样本

| 业务域 | 位置 | 声明 |
| --- | --- | --- |
| 其他 | `canvas-lms-master/config/routes.rb:23` | Rails.root.glob("{gems,vendor}/plugins/*/config/pre_routes.rb") do \|pre_routes\| |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:30` | post "/test/mock_lti/ui", to: "test/mock_lti#ui" |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:31` | post "/test/mock_lti/login", to: "test/mock_lti#login" |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:32` | get "/test/mock_lti/jwks", to: "test/mock_lti#jwks" |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:33` | post "/test/mock_lti/subscription_handler", to: "test/mock_lti#subscription_handler" |
| GraphQL API | `canvas-lms-master/config/routes.rb:36` | post "/api/graphql", to: "graphql#execute" |
| GraphQL API | `canvas-lms-master/config/routes.rb:37` | get "graphiql", to: "graphql#graphiql" |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:39` | get "acceptable_use_policy", to: "accounts#acceptable_use_policy" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:41` | resources :submissions, only: [] do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:42` | resources :submission_comments, path: :comments, only: :index, defaults: { format: :pdf } |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:43` | resources :docviewer_audit_events, only: [:create], constraints: { format: :json } |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:45` | resources :submission_comments, only: [:update, :destroy] |
| SIS、导入导出与报表 | `canvas-lms-master/config/routes.rb:47` | resources :epub_exports, only: [:index] |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:49` | get "inbox" => "context#inbox" |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:50` | get "oauth/redirect_proxy" => "oauth_proxy#redirect_proxy" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:52` | get "conversations/unread" => "conversations#index", :as => :conversations_unread, :redirect_scope => "unread" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:53` | get "conversations/starred" => "conversations#index", :as => :conversations_starred, :redirect_scope => "starred" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:54` | get "conversations/sent" => "conversations#index", :as => :conversations_sent, :redirect_scope => "sent" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:55` | get "conversations/archived" => "conversations#index", :as => :conversations_archived, :redirect_scope => "archived" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:56` | get "conversations/find_recipients" => "search#recipients" |
| 搜索、AI 与无障碍 | `canvas-lms-master/config/routes.rb:58` | get "search/recipients" => "search#recipients" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:59` | post "conversations/mark_all_as_read" => "conversations#mark_all_as_read" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:60` | get "conversations/batches" => "conversations#batches", :as => :conversation_batches |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:61` | resources :conversations, only: %i[index show update create destroy] do |
| 其他 | `canvas-lms-master/config/routes.rb:62` | post :add_recipients |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:63` | post :add_message |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:64` | post :remove_messages |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:67` | post "/external_auth_observers/redirect_login" => "login/external_auth_observers#redirect_login", :as => :external_auth_validation |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:71` | match "register/:nonce" => "communication_channels#confirm", :as => :registration_confirmation, :via => [:get, :post] |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:73` | get "pseudonyms/:id/register/:nonce" => "communication_channels#confirm", :as => :registration_confirmation_deprecated |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:74` | post "confirmations/:user_id/re_send(/:id)" => "communication_channels#re_send_confirmation", :as => :re_send_confirmation, :id => nil |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:75` | get "confirmations/:user_id/limit_reached(/:id)" => "communication_channels#confirmation_limit_reached", :as => :confirmation_limit_reached, :id => nil |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:76` | match "forgot_password" => "pseudonyms#forgot_password", :as => :forgot_password, :via => [:get, :post] |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:77` | get "pseudonyms/:pseudonym_id/change_password/:nonce" => "pseudonyms#confirm_change_password", :as => :confirm_change_password |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:78` | post "pseudonyms/:pseudonym_id/change_password/:nonce" => "pseudonyms#change_password", :as => :change_password |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:81` | get "oauth" => "users#oauth" |
| 账户与租户管理 | `canvas-lms-master/config/routes.rb:82` | get "oauth_success" => "users#oauth_success" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:84` | get "mr/:id" => "info#message_redirect", :as => :message_redirect |
| 其他 | `canvas-lms-master/config/routes.rb:85` | get "help_links" => "info#help_links" |
| 其他 | `canvas-lms-master/config/routes.rb:86` | get "ada_chat_popup" => "ada_chat_popup#show" |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:89` | get "test_error" => "info#test_error" unless Rails.env.production? |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:90` | get "live_events/heartbeat" => "info#live_events_heartbeat" unless Rails.env.production? |
| 测验与题库 | `canvas-lms-master/config/routes.rb:93` | resources :question_banks do |
| 其他 | `canvas-lms-master/config/routes.rb:94` | post :bookmark |
| 其他 | `canvas-lms-master/config/routes.rb:95` | post :reorder |
| 其他 | `canvas-lms-master/config/routes.rb:96` | get :questions |
| 其他 | `canvas-lms-master/config/routes.rb:97` | post :move_questions |
| 测验与题库 | `canvas-lms-master/config/routes.rb:98` | resources :assessment_questions |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:103` | resources :groups, except: :edit |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:104` | resources :group_categories, only: %i[create update destroy] |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:105` | get "group_unassigned_members" => "groups#unassigned_members" |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:108` | resources :group_categories do |
| 其他 | `canvas-lms-master/config/routes.rb:110` | post "clone_with_name" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:115` | get "files/folder#{full_path_glob}" => "files#react_files", :format => false, :defaults => { format: "html" } |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:116` | get "files/search" => "files#react_files", :format => false, :defaults => { format: "html" } |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:117` | resources :files, except: [:new] do |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:118` | get "download" => "files#show", :download => "1" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:119` | get "download.:type" => "files#show", :as => :typed_download, :download => "1" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:120` | get "preview" => "files#show", :preview => "1" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:121` | post "inline_view" => "files#show", :inline => "1" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:122` | get "file_preview" => "file_previews#show" |
| 其他 | `canvas-lms-master/config/routes.rb:124` | get :quota |
| 其他 | `canvas-lms-master/config/routes.rb:125` | post :reorder |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:127` | get "*file_path" => "files#show_relative", :as => :relative_path, :file_path => /.+/ # needs to stay below react_files route |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:132` | get "images" => "files#images" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:136` | get "file_contents/*file_path" => "files#show_relative", :as => :relative_file_path, :file_path => /.+/ |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:140` | resources :folders |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:144` | get "media_download" => "users#media_download" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:148` | get "users" => "context#roster" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:149` | get "user_services" => "context#roster_user_services" |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:150` | get "users/:user_id/usage" => "context#roster_user_usage", :as => :user_usage |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:151` | get "users/:id" => "context#roster_user", :as => :user |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:155` | resources :announcements |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:159` | resources :discussion_topics, only: %i[index new show edit destroy] do |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:161` | get "insights" => "discussion_topics#insights", :as => :insights |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:164` | get "discussion_topics/:id/*extras" => "discussion_topics#show", :as => :map, :extras => /.+/ |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:165` | resources :discussion_entries |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:169` | resources :wiki_pages, path: :pages, except: %i[update destroy new], constraints: { id: %r{[^/]+} } do |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:170` | get "revisions" => "wiki_pages#revisions", :as => :revisions |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:173` | get "wiki" => "wiki_pages#front_page", :as => :wiki |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:174` | get "wiki/:id" => "wiki_pages#show_redirect", :id => %r{[^/]+} |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:175` | get "wiki/:id/revisions" => "wiki_pages#revisions_redirect", :id => %r{[^/]+} |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:176` | get "wiki/:id/revisions/:revision_id" => "wiki_pages#revisions_redirect", :id => %r{[^/]+} |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:177` | get "new_page" => "wiki_pages#new", :as => :new_page |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:181` | resources :conferences do |
| 其他 | `canvas-lms-master/config/routes.rb:182` | match :join, via: [:get, :post] |
| 其他 | `canvas-lms-master/config/routes.rb:183` | match :close, via: [:get, :post] |
| 其他 | `canvas-lms-master/config/routes.rb:184` | get :recording |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:185` | delete :recording, to: "conferences#delete_recording", as: :delete_recording |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:186` | get :settings |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:190` | get "/courses/:course_id/gradebook2", to: redirect("/courses/%{course_id}/gradebook") |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:199` | resources :courses do |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:201` | get "self_enrollment/:self_enrollment" => "courses#self_enrollment", :as => :self_enrollment |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:202` | post "self_unenrollment/:self_unenrollment" => "courses#self_unenrollment", :as => :self_unenrollment |
| 其他 | `canvas-lms-master/config/routes.rb:203` | post :unconclude |
| 其他 | `canvas-lms-master/config/routes.rb:204` | get :students |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:205` | get "observer_pairing_codes.csv", action: :observer_pairing_codes_csv, as: "observer_pairing_codes" |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:206` | post :enrollment_invitation |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:208` | get "users/prior" => "context#prior_users", :as => :prior_users |
| 其他 | `canvas-lms-master/config/routes.rb:210` | get :statistics |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:211` | delete "unenroll/:id" => "courses#unenroll_user", :as => :unenroll |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:212` | post "move_enrollment/:id" => "courses#move_enrollment", :as => :move_enrollment |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:213` | delete "unenroll/:id.:format" => "courses#unenroll_user", :as => :formatted_unenroll |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:214` | post "limit_user_grading/:id" => "courses#limit_user", :as => :limit_user_grading |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:215` | delete "conclude_user/:id" => "courses#conclude_user", :as => :conclude_user_enrollment |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:216` | post "unconclude_user/:id" => "courses#unconclude_user", :as => :unconclude_user_enrollment |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:217` | resources :sections, except: %i[index edit new] do |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:218` | get "crosslist/confirm/:new_course_id" => "sections#crosslist_check", :as => :confirm_crosslist |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:219` | get "user_count" => "sections#user_count", :on => :collection, :as => :user_count |
| 其他 | `canvas-lms-master/config/routes.rb:220` | post :crosslist |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:221` | delete "crosslist" => "sections#uncrosslist", :as => :uncrosslist |
| 其他 | `canvas-lms-master/config/routes.rb:224` | get "undelete" => "context#undelete_index", :as => :undelete_items |
| 其他 | `canvas-lms-master/config/routes.rb:225` | post "undelete/:asset_string" => "context#undelete_item", :as => :undelete_item |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:227` | get "settings#{full_path_glob}", action: :settings |
| 分析、审计与运维 | `canvas-lms-master/config/routes.rb:228` | get :settings |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:229` | get "details" => "courses#settings" |
| 其他 | `canvas-lms-master/config/routes.rb:230` | post :re_send_invitations |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:231` | post :enroll_users |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:232` | post :link_enrollment |
| 其他 | `canvas-lms-master/config/routes.rb:233` | post :update_nav |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:234` | resource :gradebook do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:235` | get "submissions_upload/:assignment_id" => "gradebooks#show_submissions_upload", :as => :show_submissions_upload |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:236` | post "submissions_upload/:assignment_id" => "gradebooks#submissions_zip_upload", :as => :submissions_upload |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:239` | get :change_gradebook_version |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:240` | get :blank_submission |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:241` | get :final_grade_overrides |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:242` | get :speed_grader |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:243` | post :speed_grader_settings |
| 其他 | `canvas-lms-master/config/routes.rb:244` | get :history |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:245` | post :update_submission |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:246` | post :change_gradebook_column_size |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:247` | post :save_gradebook_column_order |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:248` | get :user_ids |
| 课程、班级与选课 | `canvas-lms-master/config/routes.rb:249` | get :grading_period_assignments |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:253` | resource :gradebook_csv, only: [:create] |
| SIS、导入导出与报表 | `canvas-lms-master/config/routes.rb:256` | get "imports/list" => "content_imports#index", :as => :import_list |
| SIS、导入导出与报表 | `canvas-lms-master/config/routes.rb:258` | get "imports" => "content_imports#intro" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:259` | resource :gradebook_upload do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:260` | get "data" => "gradebook_uploads#data" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:262` | get "grades" => "gradebooks#grade_summary", :id => nil |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:263` | get "grading_rubrics" => "gradebooks#grading_rubrics" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:264` | get "grades/:id" => "gradebooks#grade_summary", :as => :student_grades |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:265` | post "save_assignment_order" => "gradebooks#save_assignment_order", :as => :save_assignment_order |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:267` | get "calendar" => "calendars#show" |
| 其他 | `canvas-lms-master/config/routes.rb:268` | get :locks |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:270` | resources :assignments do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:271` | get "moderate" => "assignments#show_moderate" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:273` | get "anonymous_submissions/:anonymous_id", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:279` | get "anonymous_submissions/:anonymous_id", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:285` | get "anonymous_submissions/:anonymous_id", to: "anonymous_submissions#show", as: :anonymous_submission |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:287` | get "submissions/:id", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:293` | get "submissions/:id", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:299` | put "anonymous_submissions/:anonymous_id", to: "anonymous_submissions#update" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:300` | put "anonymous_submissions/:anonymous_id/reassign", to: "anonymous_submissions#redo_submission" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:301` | resources :submissions do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:302` | get "originality_report/:asset_string" => "submissions#originality_report", :as => :originality_report |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:303` | post "turnitin/resubmit" => "submissions#resubmit_to_turnitin", :as => :resubmit_to_turnitin |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:304` | get "turnitin/:asset_string" => "submissions#turnitin_report", :as => :turnitin_report |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:305` | post "vericite/resubmit" => "submissions#resubmit_to_vericite", :as => :resubmit_to_vericite |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:306` | get "vericite/:asset_string" => "submissions#vericite_report", :as => :vericite_report |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:307` | get "audit_events" => "submissions#audit_events", :as => :audit_events |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:308` | put "reassign" => "submissions#redo_submission", :as => :reassign |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:311` | get "anonymous_submissions/:anonymous_id/originality_report/:asset_string", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:314` | post "anonymous_submissions/:anonymous_id/turnitin/resubmit", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:317` | get "anonymous_submissions/:anonymous_id/turnitin/:asset_string", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:320` | post "anonymous_submissions/:anonymous_id/vericite/resubmit", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:323` | get "anonymous_submissions/:anonymous_id/vericite/:asset_string", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:327` | get :rubric |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:328` | get :rubric_data |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:329` | resource :rubric_association, path: :rubric do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:330` | resources :rubric_assessments, path: :assessments do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:332` | resources :rubric_assessment_imports, path: :imports, only: %i[create show], defaults: { format: :json } |
| SIS、导入导出与报表 | `canvas-lms-master/config/routes.rb:333` | get :export |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:338` | get :peer_reviews |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:339` | post :assign_peer_reviews |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:340` | delete "peer_reviews/:id" => "assignments#delete_peer_review", :as => :delete_peer_review |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:341` | post "peer_reviews/:id" => "assignments#remind_peer_review", :as => :remind_peer_review |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:342` | post "peer_reviews/users/:reviewer_id" => "assignments#assign_peer_review", :as => :assign_peer_review |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:343` | put "mute" => "assignments#toggle_mute" |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:346` | get :syllabus |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:349` | get "lti/resource/:resource_link_id", |
| 其他 | `canvas-lms-master/config/routes.rb:351` | action: "resource", |
| 其他 | `canvas-lms-master/config/routes.rb:354` | get :tool_launch |
| 测验与题库 | `canvas-lms-master/config/routes.rb:358` | scope(controller: :new_quizzes) do |
| 测验与题库 | `canvas-lms-master/config/routes.rb:359` | get "launch", action: :launch, as: :new_quizzes_launch |
| 测验与题库 | `canvas-lms-master/config/routes.rb:360` | get "build(/*path)", action: :launch, as: :new_quizzes_build |
| 测验与题库 | `canvas-lms-master/config/routes.rb:361` | get "reporting(/*path)", action: :launch, as: :new_quizzes_reporting |
| 测验与题库 | `canvas-lms-master/config/routes.rb:362` | get "moderation(/*path)", action: :launch, as: :new_quizzes_moderation |
| 测验与题库 | `canvas-lms-master/config/routes.rb:363` | get "exports(/*path)", action: :launch, as: :new_quizzes_exports |
| 测验与题库 | `canvas-lms-master/config/routes.rb:364` | get "taking(/*path)", action: :launch, as: :new_quizzes_taking |
| 测验与题库 | `canvas-lms-master/config/routes.rb:365` | get "observing(/*path)", action: :launch, as: :new_quizzes_observing |
| 测验与题库 | `canvas-lms-master/config/routes.rb:366` | get "errors(/*path)", action: :launch, as: :new_quizzes_errors |
| 测验与题库 | `canvas-lms-master/config/routes.rb:367` | get "settings(/*path)", action: :launch, as: :new_quizzes_settings |
| 测验与题库 | `canvas-lms-master/config/routes.rb:368` | get "version", action: :launch, as: :new_quizzes_version |
| 测验与题库 | `canvas-lms-master/config/routes.rb:369` | get "course_concluded", action: :launch, as: :new_quizzes_course_concluded |
| 测验与题库 | `canvas-lms-master/config/routes.rb:370` | get "banks(/*path)", action: :launch, as: :new_quizzes_assignment_banks |
| 测验与题库 | `canvas-lms-master/config/routes.rb:376` | scope(controller: :new_quizzes) do |
| 测验与题库 | `canvas-lms-master/config/routes.rb:377` | get "banks(/*path)", action: :banks, as: :new_quizzes_banks |
| 其他 | `canvas-lms-master/config/routes.rb:380` | resources :grading_standards, only: %i[index create update destroy] |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:382` | resources :assignment_groups do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:383` | post "reorder" => "assignment_groups#reorder_assignments", :as => :reorder_assignments |
| 其他 | `canvas-lms-master/config/routes.rb:385` | post :reorder |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:389` | get "external_tools/sessionless_launch" => "external_tools#sessionless_launch" |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:390` | resources :external_tools do |
| 其他 | `canvas-lms-master/config/routes.rb:391` | match :resource_selection, via: [:get, :post] |
| 其他 | `canvas-lms-master/config/routes.rb:392` | get :finished |
| 其他 | `canvas-lms-master/config/routes.rb:394` | get :retrieve |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:398` | get "lti/resource/:resource_link_id", |
| 其他 | `canvas-lms-master/config/routes.rb:400` | action: "resource", |
| 用户、身份与沟通 | `canvas-lms-master/config/routes.rb:402` | get "lti/basic_lti_launch_request/:message_handler_id", |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:406` | post "lti/tool_proxy_registration", controller: "lti/message", action: "registration", as: :tool_proxy_registration |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:407` | get "lti/tool_proxy_reregistration/:tool_proxy_id", |
| LTI、外部工具与集成 | `canvas-lms-master/config/routes.rb:411` | get "lti/registration_return", |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:416` | resources :submissions |
| 内容、文件与模块 | `canvas-lms-master/config/routes.rb:417` | resources :calendar_events |
| 测验与题库 | `canvas-lms-master/config/routes.rb:425` | resources :item_banks, controller: "item_banks", only: [] do |
| 测验与题库 | `canvas-lms-master/config/routes.rb:427` | get "/", to: "item_banks#show" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:428` | get "*path", to: "item_banks#show" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:432` | post "quizzes/publish"   => "quizzes/quizzes#publish" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:433` | post "quizzes/unpublish" => "quizzes/quizzes#unpublish" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:435` | post "assignments/publish/quiz"   => "assignments#publish_quizzes" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:436` | post "assignments/unpublish/quiz" => "assignments#unpublish_quizzes" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:440` | get "activity_builder/*path" => "quizzes/quizzes#index" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:441` | get "take/*path" => "quizzes/quizzes#index" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:442` | get "reports/*path" => "quizzes/quizzes#index" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:443` | get "submission-confirmation/*path" => "quizzes/quizzes#index" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:444` | get "submission-screen/*path" => "quizzes/quizzes#index" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:446` | post "quizzes/new" => "quizzes/quizzes#new" # use POST instead of GET (not idempotent) |
| 测验与题库 | `canvas-lms-master/config/routes.rb:447` | resources :quizzes, controller: "quizzes/quizzes", except: :new do |
| 测验与题库 | `canvas-lms-master/config/routes.rb:448` | get :managed_quiz_data |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:449` | get :submission_versions |
| 其他 | `canvas-lms-master/config/routes.rb:450` | get :history |
| 其他 | `canvas-lms-master/config/routes.rb:451` | get :statistics |
| 其他 | `canvas-lms-master/config/routes.rb:452` | get :read_only |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:453` | get :submission_html |
| 测验与题库 | `canvas-lms-master/config/routes.rb:455` | resources :quiz_submissions, controller: "quizzes/quiz_submissions", path: :submissions do |
| 其他 | `canvas-lms-master/config/routes.rb:457` | put :backup |
| 其他 | `canvas-lms-master/config/routes.rb:458` | post :backup |
| 其他 | `canvas-lms-master/config/routes.rb:461` | get :record_answer |
| 其他 | `canvas-lms-master/config/routes.rb:462` | post :record_answer |
| 测验与题库 | `canvas-lms-master/config/routes.rb:464` | resources :events, controller: "quizzes/quiz_submission_events", path: "log#{full_path_glob}" |
| 测验与题库 | `canvas-lms-master/config/routes.rb:467` | post "extensions/:user_id" => "quizzes/quiz_submissions#extensions", :as => :extensions |
| 测验与题库 | `canvas-lms-master/config/routes.rb:468` | resources :quiz_questions, controller: "quizzes/quiz_questions", path: :questions, only: %i[create update destroy show] |
| 测验与题库 | `canvas-lms-master/config/routes.rb:469` | resources :quiz_groups, controller: "quizzes/quiz_groups", path: :groups, only: %i[index show create update destroy] do |
| 其他 | `canvas-lms-master/config/routes.rb:471` | post :reorder |
| 测验与题库 | `canvas-lms-master/config/routes.rb:475` | match "take" => "quizzes/quizzes#show", :take => "1", :via => [:get, :post] |
| 测验与题库 | `canvas-lms-master/config/routes.rb:476` | get "take/questions/:question_id" => "quizzes/quizzes#show", :as => :question, :take => "1" |
| 其他 | `canvas-lms-master/config/routes.rb:477` | get :moderate |
| 其他 | `canvas-lms-master/config/routes.rb:478` | get :lockdown_browser_required |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:481` | resources :collaborations |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:482` | get "lti_collaborations" => "collaborations#lti_index" |
| 讨论、公告与协作 | `canvas-lms-master/config/routes.rb:483` | get "lti_collaborations/*all" => "collaborations#lti_index" |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:484` | resources :gradebook_uploads |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:485` | resources :rubrics |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:486` | post "rubrics/llm_criteria", controller: :rubrics, action: :llm_criteria |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:487` | post "rubrics/llm_regenerate_criteria", controller: :rubrics, action: :llm_regenerate_criteria |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:488` | resources :rubric_associations do |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:489` | post "remind/:assessment_request_id" => "rubric_assessments#remind", :as => :remind_assessee |
| 作业、提交与评分 | `canvas-lms-master/config/routes.rb:490` | resources :rubric_assessments, path: "assessments" |
| 学习成果与能力 | `canvas-lms-master/config/routes.rb:493` | get "outcomes/users/:user_id" => "outcomes#user_outcome_results", :as => :user_outcomes_results |

## Controller action 样本

| 业务域 | Controller | Action | 位置 |
| --- | --- | --- | --- |
| 用户、身份与沟通 | MessagesController | require_read_messages | `canvas-lms-master/app/controllers/messages_controller.rb:26` |
| 用户、身份与沟通 | MessagesController | index | `canvas-lms-master/app/controllers/messages_controller.rb:30` |
| 用户、身份与沟通 | MessagesController | show | `canvas-lms-master/app/controllers/messages_controller.rb:36` |
| 用户、身份与沟通 | MessagesController | create | `canvas-lms-master/app/controllers/messages_controller.rb:43` |
| 用户、身份与沟通 | MessagesController | html_message | `canvas-lms-master/app/controllers/messages_controller.rb:56` |
| 其他 | HistoryController | index | `canvas-lms-master/app/controllers/history_controller.rb:95` |
| 其他 | HistoryController | include_page_view? | `canvas-lms-master/app/controllers/history_controller.rb:130` |
| 作业、提交与评分 | AssignmentOverridesController | index | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:130` |
| 作业、提交与评分 | AssignmentOverridesController | show | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:140` |
| 作业、提交与评分 | AssignmentOverridesController | group_alias | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:148` |
| 作业、提交与评分 | AssignmentOverridesController | section_alias | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:163` |
| 作业、提交与评分 | AssignmentOverridesController | create | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:235` |
| 作业、提交与评分 | AssignmentOverridesController | update | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:293` |
| 作业、提交与评分 | AssignmentOverridesController | destroy | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:316` |
| 作业、提交与评分 | AssignmentOverridesController | batch_retrieve | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:341` |
| 作业、提交与评分 | AssignmentOverridesController | batch_create | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:403` |
| 作业、提交与评分 | AssignmentOverridesController | batch_update | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:439` |
| 作业、提交与评分 | AssignmentOverridesController | require_group | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:445` |
| 作业、提交与评分 | AssignmentOverridesController | require_section | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:450` |
| 作业、提交与评分 | AssignmentOverridesController | require_course | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:455` |
| 作业、提交与评分 | AssignmentOverridesController | require_assignment | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:463` |
| 作业、提交与评分 | AssignmentOverridesController | require_assignment_edit | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:467` |
| 作业、提交与评分 | AssignmentOverridesController | require_all_assignments_edit | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:471` |
| 作业、提交与评分 | AssignmentOverridesController | require_override | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:475` |
| 作业、提交与评分 | AssignmentOverridesController | bad_request | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:480` |
| 作业、提交与评分 | AssignmentOverridesController | batch_edit | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:484` |
| 测验与题库 | AssessmentQuestionBanksController | index | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:252` |
| 测验与题库 | AssessmentQuestionBanksController | show | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:273` |
| 测验与题库 | AssessmentQuestionBanksController | questions | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:294` |
| 测验与题库 | AssessmentQuestionBanksController | parse_includes | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:317` |
| 测验与题库 | AssessmentQuestionBanksController | require_context | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:321` |
| 测验与题库 | AssessmentQuestionBanksController | require_bank | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:341` |
| 讨论、公告与协作 | EportfolioCategoriesController | index | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:26` |
| 讨论、公告与协作 | EportfolioCategoriesController | create | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:36` |
| 讨论、公告与协作 | EportfolioCategoriesController | update | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:51` |
| 讨论、公告与协作 | EportfolioCategoriesController | show | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:65` |
| 讨论、公告与协作 | EportfolioCategoriesController | destroy | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:104` |
| 讨论、公告与协作 | EportfolioCategoriesController | pages | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:117` |
| 讨论、公告与协作 | EportfolioCategoriesController | eportfolio_category_params | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:138` |
| 讨论、公告与协作 | EportfolioCategoriesController | get_eportfolio | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:142` |
| 作业、提交与评分 | GradebookSettingsController | update | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:25` |
| 作业、提交与评分 | GradebookSettingsController | gradebook_settings_params | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:37` |
| 作业、提交与评分 | GradebookSettingsController | valid_colors | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:81` |
| 作业、提交与评分 | GradebookSettingsController | nilify_strings | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:85` |
| 作业、提交与评分 | GradebookSettingsController | authorize | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:100` |
| 作业、提交与评分 | GradebookSettingsController | updated_settings | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:104` |
| 作业、提交与评分 | GradebookSettingsController | deep_merge_gradebook_settings | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:113` |
| 分析、审计与运维 | JobsController | require_manage_jobs | `canvas-lms-master/app/controllers/jobs_controller.rb:27` |
| 分析、审计与运维 | JobsController | require_view_jobs | `canvas-lms-master/app/controllers/jobs_controller.rb:31` |
| 分析、审计与运维 | JobsController | index | `canvas-lms-master/app/controllers/jobs_controller.rb:35` |
| 分析、审计与运维 | JobsController | show | `canvas-lms-master/app/controllers/jobs_controller.rb:61` |
| 分析、审计与运维 | JobsController | batch_update | `canvas-lms-master/app/controllers/jobs_controller.rb:70` |
| 分析、审计与运维 | JobsController | jobs | `canvas-lms-master/app/controllers/jobs_controller.rb:88` |
| 分析、审计与运维 | JobsController | set_navigation | `canvas-lms-master/app/controllers/jobs_controller.rb:112` |
| 其他 | BlackoutDatesController | index | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:71` |
| 其他 | BlackoutDatesController | show | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:83` |
| 其他 | BlackoutDatesController | new | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:94` |
| 其他 | BlackoutDatesController | create | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:113` |
| 其他 | BlackoutDatesController | update | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:136` |
| 其他 | BlackoutDatesController | destroy | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:151` |
| 其他 | BlackoutDatesController | bulk_update | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:169` |
| 其他 | BlackoutDatesController | load_blackout_date | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:196` |
| 其他 | BlackoutDatesController | blackout_date_params | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:200` |
| 作业、提交与评分 | PeerReviewsApiController | index | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:89` |
| 作业、提交与评分 | PeerReviewsApiController | create | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:111` |
| 作业、提交与评分 | PeerReviewsApiController | destroy | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:130` |
| 作业、提交与评分 | PeerReviewsApiController | allocate | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:149` |
| 作业、提交与评分 | PeerReviewsApiController | require_assignment | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:179` |
| 作业、提交与评分 | PeerReviewsApiController | peer_review_assets | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:184` |
| 其他 | ApplicationController | instance_id | `canvas-lms-master/app/controllers/application_controller.rb:101` |
| 其他 | ApplicationController | region | `canvas-lms-master/app/controllers/application_controller.rb:105` |
| 其他 | ApplicationController | test_cluster_name | `canvas-lms-master/app/controllers/application_controller.rb:109` |
| 其他 | ApplicationController | test_cluster? | `canvas-lms-master/app/controllers/application_controller.rb:113` |
| 其他 | ApplicationController | google_drive_timeout | `canvas-lms-master/app/controllers/application_controller.rb:117` |
| 其他 | ApplicationController | batch_jobs_in_actions | `canvas-lms-master/app/controllers/application_controller.rb:123` |
| 其他 | ApplicationController | flamegraph_requested_and_permitted? | `canvas-lms-master/app/controllers/application_controller.rb:131` |
| 其他 | ApplicationController | enable_n_plus_one_detection? | `canvas-lms-master/app/controllers/application_controller.rb:139` |
| 其他 | ApplicationController | n_plus_one_detection | `canvas-lms-master/app/controllers/application_controller.rb:150` |
| 其他 | ApplicationController | generate_flamegraph | `canvas-lms-master/app/controllers/application_controller.rb:168` |
| 其他 | ApplicationController | supported_timezones | `canvas-lms-master/app/controllers/application_controller.rb:177` |
| 其他 | ApplicationController | clear_js_env | `canvas-lms-master/app/controllers/application_controller.rb:190` |
| 其他 | ApplicationController | page_has_instui_topnav | `canvas-lms-master/app/controllers/application_controller.rb:194` |
| 其他 | ApplicationController | set_normalized_route | `canvas-lms-master/app/controllers/application_controller.rb:202` |
| 其他 | ApplicationController | set_sentry_trace | `canvas-lms-master/app/controllers/application_controller.rb:212` |
| 其他 | ApplicationController | js_env | `canvas-lms-master/app/controllers/application_controller.rb:239` |
| 其他 | ApplicationController | show_career_switch? | `canvas-lms-master/app/controllers/application_controller.rb:499` |
| 其他 | ApplicationController | group_information | `canvas-lms-master/app/controllers/application_controller.rb:507` |
| 其他 | ApplicationController | cached_js_env_account_features | `canvas-lms-master/app/controllers/application_controller.rb:626` |
| 其他 | ApplicationController | js_env_root_account_settings | `canvas-lms-master/app/controllers/application_controller.rb:650` |
| 其他 | ApplicationController | cached_js_env_root_account_settings | `canvas-lms-master/app/controllers/application_controller.rb:666` |
| 其他 | ApplicationController | add_to_js_env | `canvas-lms-master/app/controllers/application_controller.rb:683` |
| 其他 | ApplicationController | render_js_env | `canvas-lms-master/app/controllers/application_controller.rb:693` |
| 其他 | ApplicationController | effective_account_attribute | `canvas-lms-master/app/controllers/application_controller.rb:700` |
| 其他 | ApplicationController | rce_js_env_base | `canvas-lms-master/app/controllers/application_controller.rb:713` |
| 其他 | ApplicationController | rce_js_env | `canvas-lms-master/app/controllers/application_controller.rb:722` |
| 其他 | ApplicationController | conditional_release_js_env | `canvas-lms-master/app/controllers/application_controller.rb:734` |
| 其他 | ApplicationController | set_student_context_cards_js_env | `canvas-lms-master/app/controllers/application_controller.rb:752` |
| 其他 | ApplicationController | external_tools_display_hashes | `canvas-lms-master/app/controllers/application_controller.rb:759` |
| 其他 | ApplicationController | external_tool_display_hash | `canvas-lms-master/app/controllers/application_controller.rb:781` |
| 其他 | ApplicationController | add_lti_tool_scopes_to_js_env | `canvas-lms-master/app/controllers/application_controller.rb:827` |
| 其他 | ApplicationController | k12? | `canvas-lms-master/app/controllers/application_controller.rb:839` |
| 其他 | ApplicationController | grading_periods? | `canvas-lms-master/app/controllers/application_controller.rb:844` |
| 其他 | ApplicationController | setup_master_course_restrictions | `canvas-lms-master/app/controllers/application_controller.rb:849` |
| 其他 | ApplicationController | set_master_course_js_env_data | `canvas-lms-master/app/controllers/application_controller.rb:862` |
| 其他 | ApplicationController | load_blueprint_courses_ui | `canvas-lms-master/app/controllers/application_controller.rb:877` |
| 其他 | ApplicationController | load_content_notices | `canvas-lms-master/app/controllers/application_controller.rb:920` |
| 其他 | ApplicationController | editing_restricted? | `canvas-lms-master/app/controllers/application_controller.rb:943` |
| 其他 | ApplicationController | tool_dimensions | `canvas-lms-master/app/controllers/application_controller.rb:950` |
| 其他 | ApplicationController | reject! | `canvas-lms-master/app/controllers/application_controller.rb:978` |
| 其他 | ApplicationController | logged_in_user | `canvas-lms-master/app/controllers/application_controller.rb:988` |
| 其他 | ApplicationController | not_fake_student_user | `canvas-lms-master/app/controllers/application_controller.rb:993` |
| 其他 | ApplicationController | rescue_action_dispatch_exception | `canvas-lms-master/app/controllers/application_controller.rb:997` |
| 其他 | ApplicationController | named_context_url | `canvas-lms-master/app/controllers/application_controller.rb:1003` |
| 其他 | ApplicationController | self | `canvas-lms-master/app/controllers/application_controller.rb:1020` |
| 其他 | ApplicationController | use_new_math_equation_handling? | `canvas-lms-master/app/controllers/application_controller.rb:1029` |
| 其他 | ApplicationController | user_url | `canvas-lms-master/app/controllers/application_controller.rb:1035` |
| 其他 | ApplicationController | increment_request_cost | `canvas-lms-master/app/controllers/application_controller.rb:1049` |
| 其他 | ApplicationController | assign_localizer | `canvas-lms-master/app/controllers/application_controller.rb:1054` |
| 其他 | ApplicationController | set_locale | `canvas-lms-master/app/controllers/application_controller.rb:1075` |
| 其他 | ApplicationController | set_timezone | `canvas-lms-master/app/controllers/application_controller.rb:1086` |
| 其他 | ApplicationController | enable_request_cache | `canvas-lms-master/app/controllers/application_controller.rb:1091` |
| 其他 | ApplicationController | batch_statsd | `canvas-lms-master/app/controllers/application_controller.rb:1095` |
| 其他 | ApplicationController | compute_http_cost | `canvas-lms-master/app/controllers/application_controller.rb:1099` |
| 其他 | ApplicationController | clear_idle_connections | `canvas-lms-master/app/controllers/application_controller.rb:1109` |
| 其他 | ApplicationController | annotate_apm | `canvas-lms-master/app/controllers/application_controller.rb:1113` |
| 其他 | ApplicationController | annotate_sentry | `canvas-lms-master/app/controllers/application_controller.rb:1122` |
| 其他 | ApplicationController | store_session_locale | `canvas-lms-master/app/controllers/application_controller.rb:1128` |
| 其他 | ApplicationController | store_session_timezone | `canvas-lms-master/app/controllers/application_controller.rb:1135` |
| 其他 | ApplicationController | init_body_classes | `canvas-lms-master/app/controllers/application_controller.rb:1141` |
| 其他 | ApplicationController | set_user_id_header | `canvas-lms-master/app/controllers/application_controller.rb:1145` |
| 其他 | ApplicationController | append_to_header | `canvas-lms-master/app/controllers/application_controller.rb:1150` |
| 其他 | ApplicationController | fix_xhr_requests | `canvas-lms-master/app/controllers/application_controller.rb:1157` |
| 其他 | ApplicationController | set_time_zone | `canvas-lms-master/app/controllers/application_controller.rb:1162` |
| 其他 | ApplicationController | load_account | `canvas-lms-master/app/controllers/application_controller.rb:1179` |
| 其他 | ApplicationController | respect_account_privacy | `canvas-lms-master/app/controllers/application_controller.rb:1185` |
| 其他 | ApplicationController | csp_frame_ancestors | `canvas-lms-master/app/controllers/application_controller.rb:1194` |
| 其他 | ApplicationController | set_pre_response_headers | `canvas-lms-master/app/controllers/application_controller.rb:1203` |
| 其他 | ApplicationController | set_response_headers | `canvas-lms-master/app/controllers/application_controller.rb:1209` |
| 其他 | ApplicationController | files_domain? | `canvas-lms-master/app/controllers/application_controller.rb:1222` |
| 其他 | ApplicationController | check_pending_otp | `canvas-lms-master/app/controllers/application_controller.rb:1226` |
| 其他 | ApplicationController | tab_enabled? | `canvas-lms-master/app/controllers/application_controller.rb:1243` |
| 其他 | ApplicationController | render_tab_disabled | `canvas-lms-master/app/controllers/application_controller.rb:1257` |
| 其他 | ApplicationController | tab_disabled_message | `canvas-lms-master/app/controllers/application_controller.rb:1270` |
| 其他 | ApplicationController | require_password_session | `canvas-lms-master/app/controllers/application_controller.rb:1283` |
| 其他 | ApplicationController | run_login_hooks | `canvas-lms-master/app/controllers/application_controller.rb:1293` |
| 其他 | ApplicationController | authorized_action | `canvas-lms-master/app/controllers/application_controller.rb:1304` |
| 其他 | ApplicationController | fix_ms_office_redirects | `canvas-lms-master/app/controllers/application_controller.rb:1320` |
| 其他 | ApplicationController | render_error_with_details | `canvas-lms-master/app/controllers/application_controller.rb:1334` |
| 其他 | ApplicationController | render_unauthorized_action | `canvas-lms-master/app/controllers/application_controller.rb:1345` |
| 其他 | ApplicationController | verified_user_check | `canvas-lms-master/app/controllers/application_controller.rb:1381` |
| 其他 | ApplicationController | render_unverified_error | `canvas-lms-master/app/controllers/application_controller.rb:1400` |
| 其他 | ApplicationController | require_context | `canvas-lms-master/app/controllers/application_controller.rb:1421` |
| 其他 | ApplicationController | require_context_and_read_access | `canvas-lms-master/app/controllers/application_controller.rb:1436` |
| 其他 | ApplicationController | require_account_context | `canvas-lms-master/app/controllers/application_controller.rb:1442` |
| 其他 | ApplicationController | require_course_context | `canvas-lms-master/app/controllers/application_controller.rb:1446` |
| 其他 | ApplicationController | require_context_type | `canvas-lms-master/app/controllers/application_controller.rb:1450` |
| 其他 | ApplicationController | get_context | `canvas-lms-master/app/controllers/application_controller.rb:1465` |
| 其他 | ApplicationController | get_all_pertinent_contexts | `canvas-lms-master/app/controllers/application_controller.rb:1555` |
| 其他 | ApplicationController | check_for_readonly_enrollment_state | `canvas-lms-master/app/controllers/application_controller.rb:1646` |
| 其他 | ApplicationController | set_badge_counts_for | `canvas-lms-master/app/controllers/application_controller.rb:1668` |
| 其他 | ApplicationController | badge_counts_for | `canvas-lms-master/app/controllers/application_controller.rb:1677` |
| 其他 | ApplicationController | content_participation_count | `canvas-lms-master/app/controllers/application_controller.rb:1688` |
| 其他 | ApplicationController | get_upcoming_assignments | `canvas-lms-master/app/controllers/application_controller.rb:1694` |
| 其他 | ApplicationController | log_course | `canvas-lms-master/app/controllers/application_controller.rb:1716` |
| 其他 | ApplicationController | get_quota | `canvas-lms-master/app/controllers/application_controller.rb:1721` |
| 其他 | ApplicationController | quota_exceeded | `canvas-lms-master/app/controllers/application_controller.rb:1728` |
| 其他 | ApplicationController | get_feed_context | `canvas-lms-master/app/controllers/application_controller.rb:1762` |
| 其他 | ApplicationController | find_user_from_uuid | `canvas-lms-master/app/controllers/application_controller.rb:1824` |
| 其他 | ApplicationController | discard_flash_if_xhr | `canvas-lms-master/app/controllers/application_controller.rb:1829` |
| 其他 | ApplicationController | cancel_cache_buster | `canvas-lms-master/app/controllers/application_controller.rb:1835` |
| 其他 | ApplicationController | cache_buster | `canvas-lms-master/app/controllers/application_controller.rb:1839` |
| 其他 | ApplicationController | initiate_session_from_token | `canvas-lms-master/app/controllers/application_controller.rb:1848` |
| 其他 | ApplicationController | remove_query_params | `canvas-lms-master/app/controllers/application_controller.rb:1903` |
| 其他 | ApplicationController | set_no_cache_headers | `canvas-lms-master/app/controllers/application_controller.rb:1913` |
| 其他 | ApplicationController | manage_robots_meta | `canvas-lms-master/app/controllers/application_controller.rb:1918` |
| 其他 | ApplicationController | set_page_view | `canvas-lms-master/app/controllers/application_controller.rb:1922` |
| 其他 | ApplicationController | require_reacceptance_of_terms | `canvas-lms-master/app/controllers/application_controller.rb:1932` |
| 其他 | ApplicationController | clear_policy_cache | `canvas-lms-master/app/controllers/application_controller.rb:1939` |
| 其他 | ApplicationController | generate_page_view | `canvas-lms-master/app/controllers/application_controller.rb:1943` |
| 其他 | ApplicationController | disable_page_views | `canvas-lms-master/app/controllers/application_controller.rb:1950` |
| 其他 | ApplicationController | update_enrollment_last_activity_at | `canvas-lms-master/app/controllers/application_controller.rb:1955` |
| 其他 | ApplicationController | log_asset_access | `canvas-lms-master/app/controllers/application_controller.rb:1970` |
| 其他 | ApplicationController | log_api_asset_access | `canvas-lms-master/app/controllers/application_controller.rb:2017` |
| 其他 | ApplicationController | log_page_view | `canvas-lms-master/app/controllers/application_controller.rb:2024` |
| 其他 | ApplicationController | add_interaction_seconds | `canvas-lms-master/app/controllers/application_controller.rb:2042` |
| 其他 | ApplicationController | log_participation | `canvas-lms-master/app/controllers/application_controller.rb:2065` |
| 其他 | ApplicationController | log_gets | `canvas-lms-master/app/controllers/application_controller.rb:2086` |
| 其他 | ApplicationController | finalize_page_view | `canvas-lms-master/app/controllers/application_controller.rb:2093` |
| 其他 | ApplicationController | rescue_expected_error_type | `canvas-lms-master/app/controllers/application_controller.rb:2124` |
| 其他 | ApplicationController | rescue_exception | `canvas-lms-master/app/controllers/application_controller.rb:2129` |
| 其他 | ApplicationController | interpret_status | `canvas-lms-master/app/controllers/application_controller.rb:2141` |
| 其他 | ApplicationController | response_code_for_rescue | `canvas-lms-master/app/controllers/application_controller.rb:2147` |
| 其他 | ApplicationController | render_optional_error_file | `canvas-lms-master/app/controllers/application_controller.rb:2151` |
| 其他 | ApplicationController | rescue_action_in_public | `canvas-lms-master/app/controllers/application_controller.rb:2161` |
| 其他 | ApplicationController | render_xhr_exception | `canvas-lms-master/app/controllers/application_controller.rb:2201` |
| 其他 | ApplicationController | render_rescue_action | `canvas-lms-master/app/controllers/application_controller.rb:2211` |
| 其他 | ApplicationController | rescue_action_in_api | `canvas-lms-master/app/controllers/application_controller.rb:2249` |
| 其他 | ApplicationController | api_error_json | `canvas-lms-master/app/controllers/application_controller.rb:2260` |
| 其他 | ApplicationController | rescue_action_locally | `canvas-lms-master/app/controllers/application_controller.rb:2296` |
| 其他 | ApplicationController | claim_session_course | `canvas-lms-master/app/controllers/application_controller.rb:2309` |
| 其他 | ApplicationController | api_request? | `canvas-lms-master/app/controllers/application_controller.rb:2326` |
| 其他 | ApplicationController | verified_file_request? | `canvas-lms-master/app/controllers/application_controller.rb:2330` |
| 其他 | ApplicationController | get_wiki_page | `canvas-lms-master/app/controllers/application_controller.rb:2336` |
| 其他 | ApplicationController | content_tag_redirect | `canvas-lms-master/app/controllers/application_controller.rb:2360` |
| 其他 | ApplicationController | set_return_url | `canvas-lms-master/app/controllers/application_controller.rb:2557` |
| 其他 | ApplicationController | lti_launch_params | `canvas-lms-master/app/controllers/application_controller.rb:2591` |
| 其他 | ApplicationController | external_tool_redirect_display_type | `canvas-lms-master/app/controllers/application_controller.rb:2596` |
| 其他 | ApplicationController | render_external_tool_prepend_template? | `canvas-lms-master/app/controllers/application_controller.rb:2607` |
| 其他 | ApplicationController | render_external_tool_append_template? | `canvas-lms-master/app/controllers/application_controller.rb:2614` |
| 其他 | ApplicationController | calendar_url_for | `canvas-lms-master/app/controllers/application_controller.rb:2623` |
| 其他 | ApplicationController | files_url_for | `canvas-lms-master/app/controllers/application_controller.rb:2635` |
| 其他 | ApplicationController | conversations_path | `canvas-lms-master/app/controllers/application_controller.rb:2657` |
| 其他 | ApplicationController | safe_domain_file_url | `canvas-lms-master/app/controllers/application_controller.rb:2672` |
| 其他 | ApplicationController | feature_enabled? | `canvas-lms-master/app/controllers/application_controller.rb:2728` |
| 其他 | ApplicationController | service_enabled? | `canvas-lms-master/app/controllers/application_controller.rb:2755` |
| 其他 | ApplicationController | feature_and_service_enabled? | `canvas-lms-master/app/controllers/application_controller.rb:2760` |
| 其他 | ApplicationController | random_lti_tool_form_id | `canvas-lms-master/app/controllers/application_controller.rb:2765` |
| 其他 | ApplicationController | temporary_user_code | `canvas-lms-master/app/controllers/application_controller.rb:2770` |
| 其他 | ApplicationController | require_account_management | `canvas-lms-master/app/controllers/application_controller.rb:2778` |
| 其他 | ApplicationController | require_root_account_management | `canvas-lms-master/app/controllers/application_controller.rb:2788` |
| 其他 | ApplicationController | require_site_admin_with_permission | `canvas-lms-master/app/controllers/application_controller.rb:2792` |
| 其他 | ApplicationController | require_context_with_permission | `canvas-lms-master/app/controllers/application_controller.rb:2796` |
| 其他 | ApplicationController | require_registered_user | `canvas-lms-master/app/controllers/application_controller.rb:2813` |
| 其他 | ApplicationController | check_incomplete_registration | `canvas-lms-master/app/controllers/application_controller.rb:2825` |
| 其他 | ApplicationController | incomplete_registration? | `canvas-lms-master/app/controllers/application_controller.rb:2831` |
| 其他 | ApplicationController | page_views_enabled? | `canvas-lms-master/app/controllers/application_controller.rb:2836` |
| 其他 | ApplicationController | verified_file_download_url | `canvas-lms-master/app/controllers/application_controller.rb:2841` |
| 其他 | ApplicationController | user_content | `canvas-lms-master/app/controllers/application_controller.rb:2852` |
| 其他 | ApplicationController | find_bank | `canvas-lms-master/app/controllers/application_controller.rb:2890` |
| 其他 | ApplicationController | in_app? | `canvas-lms-master/app/controllers/application_controller.rb:2911` |
| 其他 | ApplicationController | params_are_integers? | `canvas-lms-master/app/controllers/application_controller.rb:2915` |
| 其他 | ApplicationController | destroy_session | `canvas-lms-master/app/controllers/application_controller.rb:2924` |
| 其他 | ApplicationController | logout_current_user | `canvas-lms-master/app/controllers/application_controller.rb:2930` |
| 其他 | ApplicationController | set_layout_options | `canvas-lms-master/app/controllers/application_controller.rb:2936` |
| 其他 | ApplicationController | in_mobile_webview? | `canvas-lms-master/app/controllers/application_controller.rb:2942` |
| 其他 | ApplicationController | stringify_json_ids? | `canvas-lms-master/app/controllers/application_controller.rb:2946` |
| 其他 | ApplicationController | json_cast | `canvas-lms-master/app/controllers/application_controller.rb:2950` |
| 其他 | ApplicationController | render | `canvas-lms-master/app/controllers/application_controller.rb:2955` |
| 其他 | ApplicationController | redirect_to | `canvas-lms-master/app/controllers/application_controller.rb:2977` |
| 其他 | ApplicationController | css_bundles | `canvas-lms-master/app/controllers/application_controller.rb:2982` |
| 其他 | ApplicationController | css_bundle | `canvas-lms-master/app/controllers/application_controller.rb:2987` |
| 其他 | ApplicationController | js_bundles | `canvas-lms-master/app/controllers/application_controller.rb:2996` |
| 其他 | ApplicationController | js_bundle | `canvas-lms-master/app/controllers/application_controller.rb:3020` |
| 其他 | ApplicationController | deferred_js_bundle | `canvas-lms-master/app/controllers/application_controller.rb:3038` |
| 其他 | ApplicationController | add_body_class | `canvas-lms-master/app/controllers/application_controller.rb:3047` |
| 其他 | ApplicationController | body_classes | `canvas-lms-master/app/controllers/application_controller.rb:3055` |
| 其他 | ApplicationController | set_active_tab | `canvas-lms-master/app/controllers/application_controller.rb:3060` |
| 其他 | ApplicationController | get_active_tab | `canvas-lms-master/app/controllers/application_controller.rb:3067` |
| 其他 | ApplicationController | get_course_from_section | `canvas-lms-master/app/controllers/application_controller.rb:3072` |
| 其他 | ApplicationController | reject_student_view_student | `canvas-lms-master/app/controllers/application_controller.rb:3079` |
| 其他 | ApplicationController | check_limited_access_for_students | `canvas-lms-master/app/controllers/application_controller.rb:3086` |
| 其他 | ApplicationController | check_restricted_file_access_for_students | `canvas-lms-master/app/controllers/application_controller.rb:3101` |
| 其他 | ApplicationController | check_restricted_file_access_and_return? | `canvas-lms-master/app/controllers/application_controller.rb:3111` |
| 其他 | ApplicationController | context_account_for_student | `canvas-lms-master/app/controllers/application_controller.rb:3116` |
| 其他 | ApplicationController | context_account | `canvas-lms-master/app/controllers/application_controller.rb:3120` |
| 其他 | ApplicationController | resolve_context_account | `canvas-lms-master/app/controllers/application_controller.rb:3124` |
| 其他 | ApplicationController | set_site_admin_context | `canvas-lms-master/app/controllers/application_controller.rb:3148` |
| 其他 | ApplicationController | flash_notices | `canvas-lms-master/app/controllers/application_controller.rb:3153` |
| 其他 | ApplicationController | unsupported_browser | `canvas-lms-master/app/controllers/application_controller.rb:3184` |
| 其他 | ApplicationController | browser_supported? | `canvas-lms-master/app/controllers/application_controller.rb:3188` |

## @API 注释样本

| 业务域 | 位置 | 注释 |
| --- | --- | --- |
| 其他 | `canvas-lms-master/app/controllers/history_controller.rb:20` | # @API History |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/history_controller.rb:88` | # @API List recent history for a user |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:21` | # @API Assignments |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:124` | # @API List assignment overrides |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:135` | # @API Get a single assignment override |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:144` | # @API Redirect to the assignment override for a group |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:159` | # @API Redirect to the assignment override for a section |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:174` | # @API Create an assignment override |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:248` | # @API Update an assignment override |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:304` | # @API Delete an assignment override |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:324` | # @API Batch retrieve overrides in a course |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:372` | # @API Batch create overrides in a course |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:407` | # @API Batch update overrides in a course |
| 测验与题库 | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:21` | # @API Assessment Question Banks |
| 测验与题库 | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:234` | # @API List question banks |
| 测验与题库 | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:262` | # @API Get a single question bank |
| 测验与题库 | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:282` | # @API List assessment questions for a question bank |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:21` | # @API Blackout Dates |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:66` | # @API List blackout dates |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:78` | # @API Get a single blackout date |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:89` | # @API New Blackout Date |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:101` | # @API Create Blackout Date |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:124` | # @API Update Blackout Date |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:146` | # @API Delete Blackout Date |
| 其他 | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:158` | # @API Update a list of Blackout Dates |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:21` | # @API Peer Reviews |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:82` | # @API Get all Peer Reviews |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:104` | # @API Create Peer Review |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:123` | # @API Delete Peer Review |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:145` | # @API Allocate Peer Review |
| 其他 | `canvas-lms-master/app/controllers/tokens_controller.rb:20` | # @API Access Tokens |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/tokens_controller.rb:53` | # @API List access tokens for a user |
| 其他 | `canvas-lms-master/app/controllers/tokens_controller.rb:83` | # @API Show an access token |
| 其他 | `canvas-lms-master/app/controllers/tokens_controller.rb:96` | # @API Create an access token |
| 其他 | `canvas-lms-master/app/controllers/tokens_controller.rb:143` | # @API Update an access token |
| 其他 | `canvas-lms-master/app/controllers/tokens_controller.rb:184` | # @API Delete an access token |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:21` | # @API Account Calendars |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:107` | # @API List available account calendars |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:136` | # @API Get a single account calendar |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:154` | # @API Update a calendar |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:194` | # @API Update several calendars |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:243` | # @API List all account calendars |
| 账户与租户管理 | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:291` | # @API Count of all visible account calendars |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignments_controller.rb:21` | # @API Assignments |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignments_controller.rb:1119` | # @API Delete an assignment |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/anonymous_provisional_grades_controller.rb:21` | # @API Moderated Grading |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/anonymous_provisional_grades_controller.rb:25` | # @API Show provisional grade status for a student |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:21` | # @API Roles |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:184` | # @API List roles |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:257` | # @API Get a single role |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:278` | # @API Create a new role |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:396` | # @API Deactivate a role |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:426` | # @API Activate a role |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:476` | # @API Update a role |
| 账户与租户管理 | `canvas-lms-master/app/controllers/role_overrides_controller.rb:544` | # @API List assignable permissions |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_groups_controller.rb:21` | # @API Assignment Groups |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/assignment_groups_controller.rb:102` | # @API List assignment groups |
| 其他 | `canvas-lms-master/app/controllers/favorites_controller.rb:20` | # @API Favorites |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/favorites_controller.rb:54` | # @API List favorite courses |
| 讨论、公告与协作 | `canvas-lms-master/app/controllers/favorites_controller.rb:104` | # @API List favorite groups |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/favorites_controller.rb:128` | # @API Add course to favorites |
| 讨论、公告与协作 | `canvas-lms-master/app/controllers/favorites_controller.rb:157` | # @API Add group to favorites |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/favorites_controller.rb:184` | # @API Remove course from favorites |
| 讨论、公告与协作 | `canvas-lms-master/app/controllers/favorites_controller.rb:214` | # @API Remove group from favorites |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/favorites_controller.rb:242` | # @API Reset course favorites |
| 讨论、公告与协作 | `canvas-lms-master/app/controllers/favorites_controller.rb:256` | # @API Reset group favorites |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/epub_exports_controller.rb:21` | # @API ePub Exports |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/epub_exports_controller.rb:114` | # @API List courses with their latest ePub export |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/epub_exports_controller.rb:134` | # @API Create ePub Export |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/epub_exports_controller.rb:162` | # @API Show ePub export |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:21` | # @API Grade Change Log |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:128` | # @API Query by assignment |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:151` | # @API Query by course |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:171` | # @API Query by student |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:195` | # @API Query by grader |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:219` | # @API Advanced query |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:21` | # @API Users |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:57` | # @API List course nicknames |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:82` | # @API Get course nickname |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:99` | # @API Set course nickname |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:131` | # @API Remove course nickname |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:158` | # @API Clear course nicknames |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:21` | # @API Planner |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:93` | # @API List planner overrides |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:103` | # @API Show a planner override |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:113` | # @API Update a planner override |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:138` | # @API Create a planner override |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:184` | # @API Delete a planner override |
| 讨论、公告与协作 | `canvas-lms-master/app/controllers/external_feeds_controller.rb:21` | # @API Announcement External Feeds |
| 其他 | `canvas-lms-master/app/controllers/external_feeds_controller.rb:77` | # @API List external feeds |
| 其他 | `canvas-lms-master/app/controllers/external_feeds_controller.rb:94` | # @API Create an external feed |
| 其他 | `canvas-lms-master/app/controllers/external_feeds_controller.rb:126` | # @API Delete an external feed |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:21` | # @API Outcome Results |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:206` | # @API Get outcome results |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:255` | # @API Set outcome ordering for LMGB |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:274` | # @API Get outcome result rollups |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:392` | # @API Get contributing scores |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:458` | # @API Get mastery distribution |
| 学习成果与能力 | `canvas-lms-master/app/controllers/outcome_results_controller.rb:613` | # @API Enqueue a delayed Outcome Rollup Calculation Job |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:20` | # @API Grading Period Sets |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:51` | # @API List grading period sets |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:73` | # @API Create a grading period set |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:115` | # @API Update a grading period set |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:152` | # @API Delete a grading period set |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/tabs_controller.rb:21` | # @API Tabs |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/tabs_controller.rb:66` | # @API List available tabs for a course or group |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/tabs_controller.rb:126` | # @API Update a tab for a course |
| 分析、审计与运维 | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:21` | # @API Authentications Log |
| 账户与租户管理 | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:75` | # @API Query by login. |
| 账户与租户管理 | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:96` | # @API Query by account. |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:117` | # @API Query by user. |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:21` | # @API Grading Standards |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:95` | # @API Create a new grading standard |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:180` | # @API List the grading standards available in a context. |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:198` | # @API Get a single grading standard in a context. |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:214` | # @API Update a grading standard |
| 其他 | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:305` | # @API Delete a grading standard |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:21` | # @API Logins |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:31` | # @API List user logins |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:90` | # @API Kickoff password recovery flow |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:251` | # @API Create a user login |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:382` | # @API Edit a user login |
| 账户与租户管理 | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:493` | # @API Delete a user login |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/lmgb_user_details_controller.rb:27` | # @API Get LMGB user details |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/syllabus_api_controller.rb:26` | # @API Scan syllabus for accessibility issues |
| 内容、文件与模块 | `canvas-lms-master/app/controllers/syllabus_api_controller.rb:44` | # @API Queue syllabus accessibility scan |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/submission_comments_api_controller.rb:21` | # @API Submission Comments |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/submission_comments_api_controller.rb:30` | # @API Edit a submission comment |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/submission_comments_api_controller.rb:55` | # @API Delete a submission comment |
| 作业、提交与评分 | `canvas-lms-master/app/controllers/submission_comments_api_controller.rb:83` | # @API Upload a file |
| 其他 | `canvas-lms-master/app/controllers/scopes_api_controller.rb:21` | # @API API Token Scopes |
| 其他 | `canvas-lms-master/app/controllers/scopes_api_controller.rb:65` | # @API List scopes |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:21` | # @API Account Reports |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:210` | # @API List Available Reports |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:295` | # @API Start a Report |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:365` | # @API Index of Reports |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:384` | # @API Status of a Report |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:401` | # @API Delete a Report |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/account_reports_controller.rb:423` | # @API Abort a Report |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/comm_messages_api_controller.rb:21` | # @API CommMessages |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/comm_messages_api_controller.rb:104` | # @API List of CommMessages for a user |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/migration_issues_controller.rb:21` | # @API Content Migrations |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/migration_issues_controller.rb:101` | # @API List migration issues |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/migration_issues_controller.rb:118` | # @API Get a migration issue |
| SIS、导入导出与报表 | `canvas-lms-master/app/controllers/migration_issues_controller.rb:135` | # @API Update a migration issue |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/terms_api_controller.rb:21` | # @API Enrollment Terms |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/terms_api_controller.rb:106` | # @API List enrollment terms |
| 课程、班级与选课 | `canvas-lms-master/app/controllers/terms_api_controller.rb:212` | # @API Retrieve enrollment term |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:21` | # @API Content Shares |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:106` | # @API Create a content share |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:173` | # @API List content shares |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:195` | # @API Get unread shares count |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:212` | # @API Get content share |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:227` | # @API Remove content share |
| 用户、身份与沟通 | `canvas-lms-master/app/controllers/content_shares_controller.rb:240` | # @API Add users to content share |
| 其他 | `canvas-lms-master/app/controllers/content_shares_controller.rb:260` | # @API Update a content share |
| 账户与租户管理 | `canvas-lms-master/app/controllers/csp_settings_controller.rb:21` | # @API Content Security Policy Settings |
| 账户与租户管理 | `canvas-lms-master/app/controllers/csp_settings_controller.rb:35` | # @API Get current settings for account or course |
| 账户与租户管理 | `canvas-lms-master/app/controllers/csp_settings_controller.rb:53` | # @API Enable, disable, or clear explicit CSP setting |
| 账户与租户管理 | `canvas-lms-master/app/controllers/csp_settings_controller.rb:96` | # @API Lock or unlock current CSP settings for sub-accounts and courses |

## GraphQL 证据样本

| 业务域 | 类型 | 名称 | 位置 |
| --- | --- | --- | --- |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/patched_array_connection.rb:1` |
| 分析、审计与运维 |  |  | `canvas-lms-master/app/graphql/audit_log_field_extension.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/collection_connection.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/dynamo_connection.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/instructor_query.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/dynamo_query.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/graphql_tuning.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/graphql_helpers.rb:1` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/types.rb:1` |
| GraphQL API | class | class UnsupportedTypeError < StandardError; end | `canvas-lms-master/app/graphql/graphql_node_loader.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/instructor_connection.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/graphql_postgres_timeout.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/canvas_schema.rb:362` |
| 用户、身份与沟通 |  |  | `canvas-lms-master/app/graphql/analyzers/conversation_complexity_analyzer.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/analyzers/base_analyzer.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/analyzers/canvas_antiabuse_analyzer.rb:362` |
| GraphQL API |  |  | `canvas-lms-master/app/graphql/analyzers/log_query_complexity.rb:362` |
| GraphQL API | class | class SubmissionCommentType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/submission_comment_type.rb:39` |
| GraphQL API | class | class DraftableSubmissionType < BaseEnum | `canvas-lms-master/app/graphql/types/draftable_submission_type.rb:31` |
| GraphQL API | class | class Types::DateTimeRangeType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/date_time_range_type.rb:21` |
| GraphQL API | class | class SubmissionStatusTagType < BaseEnum | `canvas-lms-master/app/graphql/types/submission_status_tag_type.rb:22` |
| GraphQL API | class | class AccountType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/account_type.rb:22` |
| GraphQL API | class | class ModuleItemMasterCourseRestrictionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_item_master_course_restriction_type.rb:21` |
| GraphQL API | class | class GraderIdentityType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/grader_identity_type.rb:22` |
| GraphQL API | class | class AiGradeRatingType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/ai_grade_rating_type.rb:22` |
| GraphQL API | class | class SubmissionSearchFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/submission_search_filter_input_type.rb:22` |
| GraphQL API | class | class DiscussionEntryReportTypeCountsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_entry_report_type_counts_type.rb:22` |
| GraphQL API | class | class LtiAssetType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/lti_asset_type.rb:22` |
| 作业、提交与评分 | class | class SubAssignmentSubmissionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/sub_assignment_submission_type.rb:22` |
| GraphQL API | class | class RubricCriterionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/rubric_criterion_type.rb:22` |
| GraphQL API | class | class QuizType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/quiz_type.rb:23` |
| GraphQL API | class | class EnrollmentRoleType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/enrollment_type.rb:22` |
| GraphQL API | class | class NotificationType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/notification_type.rb:22` |
| GraphQL API | class | class Types::BaseField < GraphQL::Schema::Field | `canvas-lms-master/app/graphql/types/base_field.rb:21` |
| GraphQL API | class | class ModuleProgressionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_progression_type.rb:22` |
| GraphQL API | class | class NotificationPreferencesContextType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/notification_preferences_context_type.rb:22` |
| GraphQL API | class | class ExternalToolSettingsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/external_tool_settings_type.rb:22` |
| GraphQL API | class | class OutcomeAlignmentType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/outcome_alignment_type.rb:22` |
| 内容、文件与模块 | class | class ModuleItemMasteryPathInfo < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_item_mastery_path_info.rb:21` |
| GraphQL API | class | class EntryParticipantType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/entry_participant_type.rb:22` |
| GraphQL API | class | class DiscussionFilterType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/discussion_type.rb:22` |
| GraphQL API | class | class GradingStandardItemType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/grading_standard_type.rb:22` |
| GraphQL API | class | class SubmissionDraftType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/submission_draft_type.rb:22` |
| GraphQL API | class | class LockableUnionType < BaseUnion | `canvas-lms-master/app/graphql/types/lock_info_type.rb:21` |
| GraphQL API | class | class DiscussionSortOrderType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/discussion_sort_order_type.rb:22` |
| LTI、外部工具与集成 | class | class LtiAssetProcessorWindowSettingsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/lti_asset_processor_window_settings_type.rb:23` |
| GraphQL API | class | class ConversationType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/conversation_type.rb:22` |
| GraphQL API | class | class Types::UrlType < Types::BaseScalar | `canvas-lms-master/app/graphql/types/url_type.rb:21` |
| GraphQL API | class | class AssignmentTargetSortFieldInputType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/assignment_target_sort_order_input_type.rb:22` |
| GraphQL API | class | class SubmissionStatisticsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/submission_statistics_type.rb:22` |
| GraphQL API | class | class RubricAssociationType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/rubric_association_type.rb:22` |
| GraphQL API | class | class SectionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/section_type.rb:22` |
| GraphQL API | class | class AutoGradeEligibilityType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/auto_grade_eligibility_type.rb:22` |
| GraphQL API | class | class PreferredLanguageType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/preferred_language_type.rb:22` |
| LTI、外部工具与集成 | class | class LtiAssetProcessorType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/lti_asset_processor_type.rb:23` |
| GraphQL API | class | class MediaTrackType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/media_track_type.rb:22` |
| GraphQL API | class | class Types::BaseInputObject < GraphQL::Schema::InputObject | `canvas-lms-master/app/graphql/types/base_input_object.rb:3` |
| GraphQL API | class | class TermType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/term_type.rb:22` |
| 作业、提交与评分 | class | class PeerReviewSubAssignmentType < Types::AssignmentType | `canvas-lms-master/app/graphql/types/peer_review_sub_assignment_type.rb:22` |
| GraphQL API | class | class ModuleSubHeaderType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_sub_header_type.rb:23` |
| GraphQL API | class | class MessageableContextType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/messageable_context_type.rb:22` |
| GraphQL API | class | class CommentBankItemType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/comment_bank_item_type.rb:22` |
| GraphQL API | class | class QuizItemType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/quiz_item_type.rb:22` |
| GraphQL API | class | class DiscussionEntryType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_entry_type.rb:22` |
| GraphQL API | class | class ModuleCompletionStatusType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/module_completion_status_type.rb:22` |
| GraphQL API | class | class Types::ValidationErrorType < Types::ApplicationObjectType | `canvas-lms-master/app/graphql/types/validation_error_type.rb:21` |
| GraphQL API | class | class Types::InstitutionalTagCategoryType < Types::ApplicationObjectType | `canvas-lms-master/app/graphql/types/institutional_tag_category_type.rb:23` |
| GraphQL API | class | class ModuleType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_type.rb:97` |
| GraphQL API | class | class MediaSourceType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/media_source_type.rb:22` |
| GraphQL API | class | class PeerReviewDatesType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/peer_review_dates_type.rb:22` |
| LTI、外部工具与集成 | class | class LtiAssetProcessorIframeType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/lti_asset_processor_iframe_type.rb:23` |
| GraphQL API | class | class Types::BaseEnum < GraphQL::Schema::Enum | `canvas-lms-master/app/graphql/types/base_enum.rb:3` |
| GraphQL API | class | class ExternalUrlType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/external_url_type.rb:22` |
| GraphQL API | class | class FolderType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/folder_type.rb:22` |
| GraphQL API | class | class ExternalToolStateType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/external_tool_state_type.rb:22` |
| GraphQL API | class | class SubmissionHistoryType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/submission_history_type.rb:22` |
| GraphQL API | class | class ExternalToolType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/external_tool_type.rb:27` |
| GraphQL API | class | class NotificationPolicyType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/notification_policy_type.rb:22` |
| GraphQL API | class | class GradingPeriodGroupType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/grading_period_group_type.rb:22` |
| GraphQL API | class | class AiGradeResultType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/ai_grade_result_type.rb:22` |
| GraphQL API | class | class OrderDirectionType < BaseEnum | `canvas-lms-master/app/graphql/types/order_direction_type.rb:22` |
| GraphQL API | class | class GradingPeriodType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/grading_period_type.rb:22` |
| GraphQL API | class | class EnrollmentsSortFieldType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/enrollments_sort_input_type.rb:22` |
| GraphQL API | class | class AccountNotificationType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/account_notification_type.rb:22` |
| GraphQL API | class | class AssessmentType < BaseEnum | `canvas-lms-master/app/graphql/types/rubric_assessment_type.rb:22` |
| GraphQL API | class | class CoursePermissionsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/course_permissions_type.rb:22` |
| 用户、身份与沟通 | class | class ConversationMessageType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/conversation_message_type.rb:22` |
| GraphQL API | class | class SubmissionCommentFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/submission_comment_filter_input_type.rb:22` |
| 内容、文件与模块 | class | class TotalCountPageInfo < GraphQL::Types::Relay::PageInfo | `canvas-lms-master/app/graphql/types/total_count_page_info.rb:32` |
| GraphQL API | class | class NotificationPreferencesType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/notification_preferences_type.rb:22` |
| GraphQL API | class | class VericiteContextType < BaseUnion | `canvas-lms-master/app/graphql/types/vericite_data_type.rb:21` |
| GraphQL API | class | class DiscussionEntryVersionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_entry_version_type.rb:22` |
| GraphQL API | class | class StreamSummaryItemType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/activity_stream_type.rb:21` |
| GraphQL API | class | class DiscussionEntryPinningType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/discussion_entry_pinning_type.rb:22` |
| GraphQL API | class | class StringMapType < Types::BaseScalar | `canvas-lms-master/app/graphql/types/string_map_type.rb:22` |
| GraphQL API | class | class AuditEventRoleType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/audit_event_type.rb:21` |
| GraphQL API | class | class CourseOutcomeAlignmentStatsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/course_outcome_alignment_stats_type.rb:22` |
| GraphQL API | class | class CourseSettingsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/course_settings_type.rb:22` |
| 学习成果与能力 | class | class LearningOutcomeType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/learning_outcome_type.rb:22` |
| GraphQL API | class | class SubmissionFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/submission_filter_input_type.rb:22` |
| GraphQL API | class | class CustomGradeStatusType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/custom_grade_status_type.rb:21` |
| GraphQL API | class | class AssignmentType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/assignment_type.rb:26` |
| GraphQL API | class | class WidgetDashboardTypeEnum < Types::BaseEnum | `canvas-lms-master/app/graphql/types/widget_dashboard_type_enum.rb:22` |
| GraphQL API | class | class RubricRatingType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/rubric_rating_type.rb:22` |
| GraphQL API | class | class ModuleItemType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_item_type.rb:41` |
| GraphQL API | class | class PageType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/page_type.rb:22` |
| GraphQL API | class | class InboxSettingsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/inbox_settings_type.rb:22` |
| GraphQL API | class | class AuditLogsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/audit_logs_type.rb:22` |
| GraphQL API | class | class Types::AccountUsersSortFieldType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/account_users_sort_input_type.rb:21` |
| GraphQL API | class | class RecipientsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/recipients_type.rb:22` |
| GraphQL API | class | class ContentTagType < GraphQL::Types::Relay::BaseEdge | `canvas-lms-master/app/graphql/types/content_tag_type.rb:22` |
| GraphQL API | class | class ModuleFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/module_filter_input_type.rb:22` |
| GraphQL API | class | class ConversationParticipantType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/conversation_participant_type.rb:22` |
| GraphQL API | class | class Types::DateTimeType < Types::BaseScalar | `canvas-lms-master/app/graphql/types/date_time_type.rb:21` |
| GraphQL API | class | class UsageRightsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/usage_rights_type.rb:22` |
| GraphQL API | class | class SubmissionHistoryFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/submission_type.rb:28` |
| GraphQL API | class | class MutationLogType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/mutation_log_type.rb:22` |
| GraphQL API | class | class SubmissionGradingStatusType < BaseEnum | `canvas-lms-master/app/graphql/types/submission_grading_status_type.rb:22` |
| GraphQL API | class | class GradesType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/grades_type.rb:22` |
| 作业、提交与评分 | class | class AssignmentTypeEnum < Types::BaseEnum | `canvas-lms-master/app/graphql/types/assignment_type_enum.rb:22` |
| GraphQL API | class | class OutcomeCalculationMethodType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/outcome_calculation_method_type.rb:22` |
| GraphQL API | class | class DiscussionEntryPermissionsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_entry_permissions_type.rb:22` |
| GraphQL API | class | class StandardGradeStatusType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/standard_grade_status_type.rb:21` |
| GraphQL API | class | class Types::LegacyNodeType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/legacy_node_type.rb:21` |
| GraphQL API | class | class AnonymousUserType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/anonymous_user_type.rb:22` |
| GraphQL API | class | class CourseUsersSortFieldType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/course_users_sort_input_type.rb:22` |
| GraphQL API | class | class ModuleProgressionStatisticsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_progression_statistics_type.rb:22` |
| GraphQL API | class | class CheckpointType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/checkpoint_type.rb:22` |
| GraphQL API | class | class ContentTagContentType < Types::BaseUnion | `canvas-lms-master/app/graphql/types/content_tag_content_type.rb:21` |
| GraphQL API | class | class ExternalToolPlacementType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/external_tool_placement_type.rb:23` |
| 搜索、AI 与无障碍 | class | class AiFeedbackTypeEnum < Types::BaseEnum | `canvas-lms-master/app/graphql/types/ai_feedback_type_enum.rb:22` |
| GraphQL API | class | class AdhocStudentsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/assignment_override_type.rb:22` |
| GraphQL API | class | class SubmissionSearchOrderFieldInputType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/submission_search_order_input_type.rb:22` |
| GraphQL API | class | class ProficiencyRatingType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/proficiency_rating_type.rb:22` |
| 作业、提交与评分 | class | class AssignmentSubmissionType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/assignment_submission_type.rb:41` |
| GraphQL API | class | class AssignmentGroupType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/assignment_group_type.rb:21` |
| GraphQL API | class | class ModuleExternalToolType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_external_tool_type.rb:27` |
| GraphQL API | class | class AiGradeCriterionResultType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/ai_grade_criterion_result_type.rb:22` |
| GraphQL API | class | class Types::AccountUsersFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/account_users_filter_input_type.rb:21` |
| GraphQL API | class | class RubricAssessmentRatingType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/rubric_assessment_rating_type.rb:22` |
| GraphQL API | class | class GroupStateType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/group_type.rb:22` |
| GraphQL API | class | class RubricType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/rubric_type.rb:22` |
| 学习成果与能力 | class | class OutcomeProficiencyType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/outcome_proficiency_type.rb:21` |
| GraphQL API | class | class InstitutionalTagUsersSortFieldType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/institutional_tag_users_sort_input_type.rb:22` |
| GraphQL API | class | class DiscussionEntryCountsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_entry_counts_type.rb:22` |
| GraphQL API | class | class ModuleItemFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/module_item_filter_input_type.rb:22` |
| 学习成果与能力 | class | class LearningOutcomeGroupType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/learning_outcome_group_type.rb:21` |
| GraphQL API | class | class FileType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/file_type.rb:22` |
| GraphQL API | class | class SubmissionCommentStatusType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/submission_comment_status_type.rb:22` |
| GraphQL API | class | class LtiAssetReportType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/lti_asset_report_type.rb:22` |
| 作业、提交与评分 | class | class ProvisionalGradeType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/provisional_grade_type.rb:22` |
| GraphQL API | class | class AllocationRuleType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/allocation_rule_type.rb:22` |
| GraphQL API | class | class Types::SpeedGraderSettingsType < GraphQL::Schema::Object | `canvas-lms-master/app/graphql/types/speed_grader_settings_type.rb:20` |
| GraphQL API | class | class Types::BaseScalar < GraphQL::Schema::Scalar | `canvas-lms-master/app/graphql/types/base_scalar.rb:21` |
| GraphQL API | class | class GroupSetType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/group_set_type.rb:22` |
| GraphQL API | class | class MessagePermissionsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/message_permissions_type.rb:22` |
| GraphQL API | class | class Types::MutationType < Types::ApplicationObjectType | `canvas-lms-master/app/graphql/types/mutation_type.rb:31` |
| GraphQL API | class | class OutcomeFriendlyDescriptionType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/outcome_friendly_description_type.rb:22` |
| GraphQL API | class | class CourseRequirementsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/course_progression_type.rb:22` |
| GraphQL API | class | class UserGroupMembershipsFilterInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/user_group_memberships_filter_input_type.rb:22` |
| GraphQL API | class | class ModuleStatisticsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/module_statistics_type.rb:22` |
| GraphQL API | class | class CommunicationChannelType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/communication_channel_type.rb:22` |
| GraphQL API | class | class InstructorUserInfoType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/instructor_with_enrollments_type.rb:22` |
| GraphQL API | class | class ExternalToolPlacementsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/external_tool_placements_type.rb:22` |
| GraphQL API | class | class QueryType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/query_type.rb:22` |
| GraphQL API | class | class SubmissionHistoryOrderFieldInputType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/submission_history_order_input_type.rb:22` |
| GraphQL API | class | class ContentTagConnection < GraphQL::Types::Relay::BaseConnection | `canvas-lms-master/app/graphql/types/content_tag_connection.rb:21` |
| GraphQL API | class | class Types::InstitutionalTagAssociationType < Types::ApplicationObjectType | `canvas-lms-master/app/graphql/types/institutional_tag_association_type.rb:23` |
| GraphQL API | class | class TurnitinDataType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/turnitin_data_type.rb:22` |
| GraphQL API | class | class ScheduledPostType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/scheduled_post_type.rb:22` |
| GraphQL API | class | class DiscussionPermissionsType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/discussion_permissions_type.rb:22` |
| GraphQL API | class | class GroupMembershipStateType < Types::BaseEnum | `canvas-lms-master/app/graphql/types/group_membership_type.rb:22` |
| GraphQL API | class | class DateHashSetType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/date_hash_type.rb:22` |
| GraphQL API | class | class SubmissionStateType < BaseEnum | `canvas-lms-master/app/graphql/types/submission_state_type.rb:33` |
| GraphQL API | class | class AssessmentRequestType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/assessment_request_type.rb:22` |
| GraphQL API | class | class CourseDashboardCardLinkType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/course_dashboard_card_type.rb:21` |
| GraphQL API | class | class ProficiencyRatingInputType < Types::BaseInputObject | `canvas-lms-master/app/graphql/types/proficiency_rating_input_type.rb:22` |
| GraphQL API | class | class PostPolicyType < ApplicationObjectType | `canvas-lms-master/app/graphql/types/post_policy_type.rb:22` |
| GraphQL API | class | class SubmissionPostingStatusType < BaseEnum | `canvas-lms-master/app/graphql/types/submission_posting_status_type.rb:22` |
| GraphQL API | class | class Types::BaseUnion < GraphQL::Schema::Union | `canvas-lms-master/app/graphql/types/base_union.rb:20` |

## 说明
Rails 路由有大量嵌套 context，例如 `/courses/:course_id/...` 和 `/accounts/:account_id/...`。实际权限和上下文解析通常在 Controller before_action、Model policy 和 Canvas 自定义权限层中完成。
