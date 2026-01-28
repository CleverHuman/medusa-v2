# Telegram Policy Configuration Management Page

## Feature Overview

This management page is used to configure the policy content of Telegram Bot, including:

- **Privacy Policy**: Privacy policy content for users
- **Terms of Service**: Terms and conditions for service usage
- **Refund Policy**: Refund and return policy information
- **Shipping Policy**: Shipping and delivery policy details
- **Contact Information**: Customer support contact details

## Page Structure

### Left Side: Configuration Form
- Privacy Policy textarea (6 rows)
- Terms of Service textarea (6 rows)
- Refund Policy textarea (6 rows)
- Shipping Policy textarea (6 rows)
- Contact Information textarea (4 rows)

### Right Side: Real-time Preview
- Simulated Telegram Bot interface
- Real-time display of policy content
- Preview with emoji icons and formatted sections

## Data Format

Configuration data uses JSON format:

```json
{
  "privacy_policy": "We respect your privacy and are committed to protecting your personal data...",
  "terms_of_service": "By using our services, you agree to these terms and conditions...",
  "refund_policy": "We offer refunds within 7 days of purchase for digital products...",
  "shipping_policy": "We ship worldwide with discreet packaging...",
  "contact_info": "For support, please contact us at support@yourdomain.com..."
}
```

## API Interfaces

### Get Configuration
- **URL**: `/admin/mall/tg-policy-config`
- **Method**: GET
- **Permission**: `mall:tg-policy-config:query`

### Save Configuration
- **URL**: `/admin/mall/tg-policy-config`
- **Method**: POST
- **Permission**: `mall:tg-policy-config:add`

### Update Configuration
- **URL**: `/admin/mall/tg-policy-config`
- **Method**: PUT
- **Permission**: `mall:tg-policy-config:edit`

## Database Table Structure

Table name: `tg_policy_config`

| Field Name | Type | Description |
|------------|------|-------------|
| id | BIGINT | Primary key, auto-increment |
| privacy_policy | TEXT | Privacy policy content |
| terms_of_service | TEXT | Terms of service content |
| refund_policy | TEXT | Refund policy content |
| shipping_policy | TEXT | Shipping policy content |
| contact_info | TEXT | Contact information |
| create_time | DATETIME | Creation time |
| update_time | DATETIME | Update time |
| update_by | VARCHAR(64) | Updated by |
| remark | VARCHAR(255) | Remarks |

## Usage Instructions

1. **Access Page**: Navigate to "Mall Management" -> "Telegram Policy Config" in the admin backend
2. **Configure Content**: Fill in various policy configurations in the left form
3. **Real-time Preview**: The right side will display policy content in real-time
4. **Save Configuration**: Click "Save Configuration" button to save changes

## Notes

- All policy content supports multi-line text input
- Content is displayed with emoji icons in preview
- Configuration will be automatically saved to database
- Supports real-time preview functionality
- Text areas support scrolling for long content 