# Vendor Module Setup Guide

## Overview

This guide provides step-by-step instructions for setting up the Vendor Management module in the Medusa system. The module allows managing vendor applications and approved vendors independently.

## Module Structure

```
medusa-mall/
├── src/main/java/com/medusa/mall/
│   ├── domain/vendor/              # Entity classes
│   │   ├── VendorApplication.java
│   │   └── Vendor.java
│   ├── mapper/                     # Mapper interfaces
│   │   ├── VendorApplicationMapper.java
│   │   └── VendorMapper.java
│   ├── service/                    # Service interfaces
│   │   ├── IVendorApplicationService.java
│   │   └── IVendorService.java
│   ├── service/impl/               # Service implementations
│   │   ├── VendorApplicationServiceImpl.java
│   │   └── VendorServiceImpl.java
│   └── controller/                 # REST controllers
│       ├── VendorApplicationController.java
│       └── VendorController.java
└── src/main/resources/mapper/mall/
    ├── VendorApplicationMapper.xml
    └── VendorMapper.xml

medusa-admin-vue/
└── src/
    ├── api/mall/
    │   └── vendor.js                # API definitions
    └── views/mall/vendor/
        ├── application/
        │   └── index.vue            # Application management page
        └── list/
            └── index.vue            # Vendor list page
```

## Installation Steps

### Step 1: Database Setup

Execute the SQL script to create the necessary tables:

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/sql
mysql -u [username] -p [database_name] < create_vendor_module.sql
```

Or manually execute the SQL in your database management tool.

The script creates three tables:
- `mall_vendor_application` - Stores vendor applications
- `mall_vendor` - Stores approved vendor information
- `mall_vendor_review_history` - Stores review history

### Step 2: Backend Deployment

The backend code has been created in the following locations:

1. **Domain Entities**: `medusa-mall/src/main/java/com/medusa/mall/domain/vendor/`
2. **Mappers**: `medusa-mall/src/main/java/com/medusa/mall/mapper/`
3. **Services**: `medusa-mall/src/main/java/com/medusa/mall/service/` and `service/impl/`
4. **Controllers**: `medusa-mall/src/main/java/com/medusa/mall/controller/`
5. **XML Mappings**: `medusa-mall/src/main/resources/mapper/mall/`

**Build and deploy:**

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3
mvn clean install
# Restart your application server
```

### Step 3: Frontend Deployment

The frontend code has been created:

1. **API Module**: `medusa-admin-vue/src/api/mall/vendor.js`
2. **Vue Pages**:
   - Application Management: `medusa-admin-vue/src/views/mall/vendor/application/index.vue`
   - Vendor List: `medusa-admin-vue/src/views/mall/vendor/list/index.vue`

**Build and deploy:**

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/medusa-admin-vue
npm install  # If not already installed
npm run build:prod
# Deploy the dist folder to your web server
```

### Step 4: Menu Configuration

Add menu entries in the system admin panel or directly in the `sys_menu` table:

```sql
-- Parent menu (adjust parent_id based on your Mall menu structure)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
('Vendor Management', [MALL_MENU_ID], 6, 'vendor', NULL, 1, 0, 'M', '0', '0', NULL, 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor module menu');

-- Get the ID of the above menu and use it as parent_id for these:
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
('Vendor Application', [VENDOR_MENU_ID], 1, 'application', 'mall/vendor/application/index', 1, 0, 'C', '0', '0', 'mall:vendor:application:list', 'form', 'admin', NOW(), 'admin', NOW(), 'Vendor application management'),
('Vendor List', [VENDOR_MENU_ID], 2, 'list', 'mall/vendor/list/index', 1, 0, 'C', '0', '0', 'mall:vendor:list', 'peoples', 'admin', NOW(), 'admin', NOW(), 'Vendor list management');

-- Add button permissions for Vendor Application
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Application', [APPLICATION_MENU_ID], 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:query', '#', 'admin', NOW()),
('Add Application', [APPLICATION_MENU_ID], 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:add', '#', 'admin', NOW()),
('Edit Application', [APPLICATION_MENU_ID], 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:edit', '#', 'admin', NOW()),
('Delete Application', [APPLICATION_MENU_ID], 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:remove', '#', 'admin', NOW()),
('Approve Application', [APPLICATION_MENU_ID], 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:approve', '#', 'admin', NOW()),
('Reject Application', [APPLICATION_MENU_ID], 6, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:reject', '#', 'admin', NOW()),
('Export Application', [APPLICATION_MENU_ID], 7, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:application:export', '#', 'admin', NOW());

-- Add button permissions for Vendor List
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('Query Vendor', [VENDOR_LIST_MENU_ID], 1, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:query', '#', 'admin', NOW()),
('Add Vendor', [VENDOR_LIST_MENU_ID], 2, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:add', '#', 'admin', NOW()),
('Edit Vendor', [VENDOR_LIST_MENU_ID], 3, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:edit', '#', 'admin', NOW()),
('Delete Vendor', [VENDOR_LIST_MENU_ID], 4, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:remove', '#', 'admin', NOW()),
('Export Vendor', [VENDOR_LIST_MENU_ID], 5, '#', '', 1, 0, 'F', '0', '0', 'mall:vendor:export', '#', 'admin', NOW());
```

### Step 5: Role Permissions

Assign the vendor module permissions to appropriate roles in the system:

1. Login to admin panel
2. Navigate to System Management > Role Management
3. Edit the role that needs vendor management access
4. Check the permissions for:
   - Vendor Management
   - Vendor Application (with all sub-permissions)
   - Vendor List (with all sub-permissions)
5. Save the role configuration

## Features

### Vendor Application Management

**Path**: Mall > Vendor Management > Vendor Application

**Features**:
- View all vendor applications with filtering by status, location, vendor name
- View detailed application information including:
  - Basic information (name, location, experience)
  - Contact methods (Telegram, Signal, Jabber, Email)
  - Wallet addresses (BTC, XMR, USDT)
  - PGP signature URL
  - Product categories and descriptions
- Approve/Reject applications with review notes
- Track review progress (0-100%)
- Export application data to Excel

**Application Statuses**:
- `pending` - Newly submitted, awaiting review
- `under_review` - Currently being reviewed
- `interview_required` - Interview needed before approval
- `approved` - Application approved, vendor created
- `rejected` - Application rejected

### Vendor List Management

**Path**: Mall > Vendor Management > Vendor List

**Features**:
- View all approved vendors with filtering
- Add/Edit vendor information
- Enable/Disable vendor status
- Mark vendors as featured
- Manage vendor contact information
- View vendor statistics (total orders, rating)
- Export vendor data to Excel

## API Endpoints

### Vendor Application APIs

- `GET /admin/mall/vendor/application/list` - Get application list
- `GET /admin/mall/vendor/application/{id}` - Get application details
- `POST /admin/mall/vendor/application` - Create application
- `PUT /admin/mall/vendor/application` - Update application
- `DELETE /admin/mall/vendor/application/{ids}` - Delete applications
- `POST /admin/mall/vendor/application/approve/{id}` - Approve application
- `POST /admin/mall/vendor/application/reject/{id}` - Reject application
- `POST /admin/mall/vendor/application/progress/{id}` - Update review progress
- `POST /admin/mall/vendor/application/export` - Export applications

### Vendor Management APIs

- `GET /admin/mall/vendor/list` - Get vendor list
- `GET /admin/mall/vendor/{id}` - Get vendor details
- `POST /admin/mall/vendor` - Create vendor
- `PUT /admin/mall/vendor` - Update vendor
- `DELETE /admin/mall/vendor/{ids}` - Delete vendors
- `POST /admin/mall/vendor/export` - Export vendors

## Integration with Existing System

### Product Origin Integration

The vendor module integrates with the existing product system through the `product_origin` and `origin_id` fields in the `mall_product` table:

- When `product_origin = 1`, the product is from a vendor
- The `origin_id` field stores the vendor's ID from the `mall_vendor` table

### Linking Products to Vendors

When creating or editing products:

1. Set `product_origin = 1` (vendor product)
2. Set `origin_id` to the vendor's ID
3. The product will be associated with that vendor

Example:
```sql
UPDATE mall_product 
SET product_origin = 1, origin_id = 123 
WHERE id = 456;
```

## Module Independence

The vendor module is designed to be independent:

1. **Database**: All tables use `mall_vendor_` prefix
2. **Code**: All code is in the `vendor` sub-package
3. **API**: All endpoints use `/admin/mall/vendor/` prefix
4. **Frontend**: All pages are in `views/mall/vendor/` directory
5. **Permissions**: All permissions use `mall:vendor:` prefix

This makes it easy to:
- Maintain the module separately
- Remove the module if needed
- Update the module without affecting other parts
- Extend the module with additional features

## Troubleshooting

### Database Issues

**Problem**: Tables not created
**Solution**: Check MySQL user permissions, ensure CREATE TABLE privilege

**Problem**: Foreign key errors
**Solution**: Ensure proper table order in SQL execution

### Backend Issues

**Problem**: Mapper not found
**Solution**: Check MyBatis mapper scanning configuration in application.yml

**Problem**: Permission denied
**Solution**: Check sys_menu table entries and role permissions

### Frontend Issues

**Problem**: Page not displaying
**Solution**: Check router configuration and menu permissions

**Problem**: API calls failing
**Solution**: Verify backend API endpoints are accessible and CORS is configured

## Support

For issues or questions:
1. Check the logs: `medusa-admin/logs/` and browser console
2. Verify database connections and permissions
3. Ensure all files are properly deployed
4. Check firewall and network settings

## Future Enhancements

Potential improvements for the vendor module:

1. **Vendor Portal**: Public-facing portal for vendors to submit applications
2. **Multi-step Review**: Advanced workflow with multiple review stages
3. **Vendor Dashboard**: Analytics and reporting for vendors
4. **Commission Management**: Track vendor commissions and payouts
5. **Product Approval**: Review process for vendor-submitted products
6. **Vendor Ratings**: Customer reviews and ratings for vendors
7. **Automated Notifications**: Email/SMS notifications for application status changes
8. **Document Upload**: Support for uploading vendor documents and certifications

## License

This module follows the same license as the main Medusa project.

