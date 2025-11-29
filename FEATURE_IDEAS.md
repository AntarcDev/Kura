# Kura - Feature Ideas & Roadmap

This document contains potential features and improvements for the Kura app.

## High Priority Features

### 1. Pull-to-Refresh [DONE]
- **Description**: Add swipe-down to refresh on creator and post lists
- **Benefits**: Quick way to get latest content without restarting
- **Effort**: Low
- **Impact**: High

### 2. Offline Mode [DONE]
- **Description**: Cache posts and images for offline viewing
- **Features**:
  - Show cached content when network is unavailable
  - Sync when connection is restored
  - Cache size management
- **Effort**: Medium
- **Impact**: High

### 3. Image Gallery Viewer [DONE]
- **Description**: Full-screen image viewer with gestures
- **Features**:
  - Zoom/pinch gestures
  - Swipe between images in a post
  - Share/save individual images
- **Effort**: Medium
- **Impact**: High

### 4. Download Manager UI [DONE]
- **Description**: Visual interface for download management
- **Features**:
  - Show active downloads with progress bars
  - Download queue management
  - Notification when downloads complete
  - Pause/resume/cancel downloads
- **Effort**: Medium
- **Impact**: Medium

### 5. Filter & Sort [DONE]
- **Description**: Enhanced content organization
- **Features**:
  - Sort creators by name, recent updates, favorites
  - Filter posts by date, tags, or content type
  - Service filter (Patreon, Fanbox, SubscribeStar, etc.)
- **Effort**: Medium
- **Impact**: High

---

## Medium Priority Features

### 6. Dark/Light Theme Toggle [DONE]
- **Description**: Manual theme selection
- **Features**:
  - Manual theme selection in settings
  - Follow system theme option
  - Custom accent colors
- **Effort**: Low
- **Impact**: Medium

### 7. Post Bookmarks
- **Description**: Bookmark individual posts (separate from creator favorites)
- **Features**:
  - Bookmarks screen to view saved posts
  - Quick access to saved content
  - Bookmark organization/tags
- **Effort**: Medium
- **Impact**: Medium

### 8. Search Enhancements [DONE]
- **Description**: Improved search capabilities
- **Features**:
  - Search within post content
  - Search by tags
  - Recent searches history
  - Search suggestions
- **Effort**: Medium
- **Impact**: Medium

### 9. Notifications
- **Description**: Push notifications for new content
- **Features**:
  - Notify when favorite creators post new content
  - Configurable notification preferences
  - Background sync with WorkManager
  - Quiet hours setting
- **Effort**: High
- **Impact**: High

### 10. Grid/List View Toggle [DONE]
- **Description**: Multiple layout options
- **Features**:
  - Switch between grid and list layouts for posts
  - Compact view option for browsing more content
  - Remember preference per screen
- **Effort**: Low
- **Impact**: Medium

---

## Polish & UX Improvements

### 11. Loading Skeletons [DONE]
- **Description**: Better loading states
- **Features**:
  - Shimmer loading placeholders instead of spinners
  - Better visual feedback while loading
  - Smooth transitions
- **Effort**: Low
- **Impact**: Medium

### 12. Error Handling [DONE]
- **Description**: Improved error experience
- **Features**:
  - Retry buttons on error screens
  - Better error messages with suggestions
  - Offline indicator
  - Network status banner
- **Effort**: Low
- **Impact**: Medium

### 13. Post Sharing [PARTIAL]
- **Description**: Share content with others
- **Features**:
  - Share post links
  - Share images directly
  - Copy post content
  - Share to social media
- **Effort**: Low
- **Impact**: Low

### 14. Creator Profiles [DONE]
- **Description**: Enhanced creator information
- **Features**:
  - Show creator bio/description
  - Total post count
  - Join date, platform links
  - Social media links
- **Effort**: Medium
- **Impact**: Medium

### 15. Video Support [PARTIAL]
- **Description**: Handle video content
- **Features**:
  - Play embedded videos
  - Download video files [DONE]
  - Video thumbnail previews [DONE]
  - Picture-in-picture mode
- **Effort**: High
- **Impact**: High

---

## Advanced Features

### 16. Multi-Account Support
- **Description**: Manage multiple accounts
- **Features**:
  - Switch between different kemono.cr accounts
  - Separate favorites per account
  - Account-specific settings
- **Effort**: High
- **Impact**: Low

### 17. Export/Import
- **Description**: Data portability
- **Features**:
  - Export favorites list
  - Import from backup
  - Sync across devices
  - JSON/CSV export formats
- **Effort**: Medium
- **Impact**: Low

### 18. Advanced Filters [PARTIAL]
- **Description**: Granular content filtering
- **Features**:
  - Filter by NSFW/SFW
  - Exclude certain tags
  - Custom filter rules
  - Blacklist/whitelist
- **Effort**: Medium
- **Impact**: Medium

### 19. Statistics
- **Description**: Usage analytics
- **Features**:
  - Track viewing history
  - Most viewed creators
  - Download statistics
  - Time spent in app
- **Effort**: Medium
- **Impact**: Low

### 20. Widget Support
- **Description**: Home screen widgets
- **Features**:
  - Widget showing latest posts
  - Quick access to favorites
  - Customizable widget layouts
- **Effort**: High
- **Impact**: Low

---

## Recommended Implementation Order

### Phase 1 - Quick Wins (Completed)
1. Pull-to-Refresh [DONE]
2. Grid/List View Toggle [DONE]
3. Loading Skeletons [DONE]
4. Error Handling Improvements [DONE]

### Phase 2 - Core Features (Completed)
5. Image Gallery Viewer [DONE]
6. Download Manager UI [DONE]
7. Filter & Sort [DONE]
8. Dark/Light Theme Toggle [DONE]

### Phase 3 - Advanced Features (In Progress)
9. Offline Mode [DONE]
10. Post Bookmarks
11. Search Enhancements [DONE]
12. Notifications

### Phase 4 - Polish & Extras (ongoing)
13. Video Support [PARTIAL]
14. Creator Profiles [DONE]
15. Post Sharing [PARTIAL]
16. Advanced Filters [PARTIAL]

---

## Notes

- Features are prioritized based on effort vs. impact
- Implementation order can be adjusted based on user feedback
- Some features may require API changes or additional permissions
- Performance testing should be done after implementing caching/offline features
