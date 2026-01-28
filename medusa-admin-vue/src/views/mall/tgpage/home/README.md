# Telegram Home Configuration Management Page

## Feature Overview

This management page is used to configure the home page content of Telegram Bot, including:

- **Banner Image**: Image displayed at the top of the home page
- **Page Title**: Main title of the home page
- **Page Description**: Description text of the home page
- **Button Configuration**: Configurable button list, each button contains label and action

## Page Structure

### Left Side: Configuration Form
- Banner image URL input box (with preview)
- Page title input box
- Page description multi-line text box
- Button configuration area (can dynamically add/delete buttons)

### Right Side: Real-time Preview
- Simulated Telegram Bot interface
- Real-time display of configuration effects
- Preview including Banner, title, description and buttons

## Data Format

Configuration data uses JSON format:

```json
{
  "banner_image": "https://yourdomain.com/profile/upload/2025/07/26/home_banner.jpg",
  "title": "Welcome to Telegram Store",
  "description": "We support BTC / USDT / XMR payment, global anonymous direct shipping!",
  "buttons": [
    { "label": "🛍 Browse Products", "action": "browse_products" },
    { "label": "📦 My Orders", "action": "view_orders" },
    { "label": "🎁 Limited Offers", "action": "promo" }
  ]
}
```

## API Interfaces

### Get Configuration
- **URL**: `/admin/mall/tg-home-config`
- **Method**: GET
- **Permission**: `mall:tg-home-config:query`

### Save Configuration
- **URL**: `/admin/mall/tg-home-config`
- **Method**: POST
- **Permission**: `mall:tg-home-config:add`

### Update Configuration
- **URL**: `/admin/mall/tg-home-config`
- **Method**: PUT
- **Permission**: `mall:tg-home-config:edit`

## Database Table Structure

Table name: `tg_home_config`

| Field Name | Type | Description |
|------------|------|-------------|
| id | BIGINT | Primary key, auto-increment |
| banner_image | VARCHAR(500) | Banner image URL |
| title | VARCHAR(200) | Page title |
| description | TEXT | Page description |
| buttons | JSON | Button configuration JSON array |
| create_time | DATETIME | Creation time |
| update_time | DATETIME | Update time |
| update_by | VARCHAR(64) | Updated by |
| remark | VARCHAR(255) | Remarks |

## Usage Instructions

1. **Access Page**: Navigate to "Mall Management" -> "Telegram Home Config" in the admin backend
2. **Configure Content**: Fill in various configurations in the left form
3. **Real-time Preview**: The right side will display configuration effects in real-time
4. **Save Configuration**: Click "Save Configuration" button to save changes
5. **Button Management**: Can add, edit, delete button configurations

## Notes

- Banner image URL needs to be a complete accessible URL
- Button labels support emoji expressions
- Button actions are used for Telegram Bot command processing
- Configuration will be automatically saved to database
- Supports real-time preview functionality 