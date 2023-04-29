# Telegram W

Telegram client for Wear OS based on tdlib and Compose.

Note that this is by no means a fully functioning Telegram client,
as many features are still either missing or unstable.

Fully or partially implemented features include:
- Login (with either a phone number + authentication code, or QR code)
- View private and group chats (including forum topics)
- View user and group information
- Send text messages
- View image, video, animation, location, and sticker messages
- Listen to audio messages
- Notifications (can be toggled group by group)
- Reactions

Missing (for now) features include:
- Send audio, sticker, and location messages

## Building

Building should work on at least Android Studio 2022.1.1 Canary 9.

You should enter your Telegram API access details in `local.properties` with keys `TELEGRAM_API_ID`, `TELEGRAM_API_HASH`.
if you intend to use the app for more than just testing or development.

## Known issues
- Chats are sometimes fetched incorrectly or with missing information in the main view
- Occasional poor performance
