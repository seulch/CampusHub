

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── campuseventhub/
│   │           ├── Main.java
│   │           ├── config/
│   │           │   └── ApplicationConfig.java
│   │           ├── model/
│   │           │   ├── user/
│   │           │   │   ├── User.java
│   │           │   │   ├── Organizer.java
│   │           │   │   ├── Attendee.java
│   │           │   │   └── Admin.java
│   │           │   ├── event/
│   │           │   │   ├── Event.java
│   │           │   │   ├── EventType.java
│   │           │   │   ├── EventStatus.java
│   │           │   │   └── Registration.java
│   │           │   ├── venue/
│   │           │   │   └── Venue.java
│   │           │   ├── notification/
│   │           │   │   ├── Notification.java
│   │           │   │   └── NotificationType.java
│   │           │   └── report/
│   │           │       ├── Report.java
│   │           │       ├── EventReport.java
│   │           │       └── SystemReport.java
│   │           ├── service/
│   │           │   ├── EventHub.java
│   │           │   ├── UserManager.java
│   │           │   ├── EventManager.java
│   │           │   ├── VenueManager.java
│   │           │   ├── NotificationService.java
│   │           │   └── NotificationTemplateManager.java - Loads and retrieves notification templates
│   │           ├── strategy/
│   │           │   ├── NotificationStrategy.java
│   │           │   ├── EmailNotification.java
│   │           │   ├── SMSNotification.java
│   │           │   └── InAppNotification.java
│   │           ├── gui/
│   │           │   ├── LoginFrame.java
│   │           │   ├── organizer/
│   │           │   │   ├── OrganizerDashboard.java
│   │           │   │   ├── EventCreationWizard.java
│   │           │   │   └── EventManagementPanel.java
│   │           │   ├── attendee/
│   │           │   │   ├── AttendeeDashboard.java
│   │           │   │   ├── EventBrowserPanel.java
│   │           │   │   └── RegistrationPanel.java
│   │           │   ├── admin/
│   │           │   │   ├── AdminDashboard.java
│   │           │   │   ├── UserManagementPanel.java
│   │           │   │   └── SystemReportsPanel.java
│   │           │   └── common/
│   │           │       ├── BaseFrame.java
│   │           │       └── ComponentFactory.java
│   │           ├── util/
│   │           │   ├── DateTimeUtil.java
│   │           │   ├── ValidationUtil.java
│   │           │   ├── FileUtil.java
│   │           │   └── QRCodeGenerator.java
│   │           └── persistence/
│   │               ├── DataManager.java
│   │               ├── UserRepository.java
│   │               ├── EventRepository.java
│   │               └── VenueRepository.java
│   └── resources/
│       ├── data/
│       │   ├── users.ser
│       │   ├── events.ser
│       │   └── venues.ser
│       ├── config/
│       │   └── application.properties
│       └── icons/
│           └── (GUI icons and images)
└── test/
    └── java/
        └── com/
            └── campuseventhub/
                ├── model/
                ├── service/
                └── util/

