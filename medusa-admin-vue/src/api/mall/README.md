# Mall API Documentation

## Authentication
All API endpoints require authentication except those marked with `@Anonymous`. Include the authentication token in the request header:
```
Authorization: Bearer <token>
```

## Common Response Format
All API responses follow this format:
```json
{
  "code": "number",    // 200 for success, other codes for errors
  "msg": "string",     // Success or error message
  "data": "object"     // Response data (if any)
}
```

## Error Codes
- 200: Success
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

## Member Management

### Register
- **URL**: `/api/mall/register`
- **Method**: `POST`
- **Description**: Register a new member
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
    "sourceType": "number"  // 0: OS, 1: Telegram
  }
  ```

### Login
- **URL**: `/api/mall/login`
- **Method**: `POST`
- **Description**: Login with member credentials
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
    "sourceType": "number"  // 0: OS, 1: Telegram
  }
  ```
- **Response**:
  ```json
  {
    "code": 200,
    "token": "string"
  }
  ```

### Get Member Info
- **URL**: `/api/mall/getMemberInfo`
- **Method**: `GET`
- **Description**: Get current member's information
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "memberId": "number",
      "username": "string",
      "level": "number",
      "benefits": {
        "fixedDiscount": "number"
      }
    }
  }
  ```

### Get Member Benefits
- **URL**: `/api/mall/getMemberBenefit`
- **Method**: `GET`
- **Description**: Get member's benefits information

## Order Management

### Get Order List
- **URL**: `/api/mall/order/list`
- **Method**: `GET`
- **Description**: Get current member's order list with pagination
- **Response**:
  ```json
  {
    "code": 200,
    "rows": [
      {
        "id": "string",
        "orderSn": "string",
        "status": "number",
        "createTime": "string",
        "totalAmount": "number",
        "shipping": {
          "shippingNumber": "string",
          "shippingMethod": "string"
        }
      }
    ],
    "total": "number"
  }
  ```

### Create Order
- **URL**: `/api/mall/order/add`
- **Method**: `POST`
- **Description**: Create a new order
- **Request Body**:
  ```json
  {
    "items": [
      {
        "productId": "number",
        "quantity": "number"
      }
    ],
    "shippingAddress": {
      "address": "string",
      "city": "string",
      "country": "string",
      "phone": "string"
    }
  }
  ```

### Get Order Detail
- **URL**: `/api/mall/order/{id}`
- **Method**: `GET`
- **Description**: Get detailed information about a specific order
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "order": {
        "id": "string",
        "orderSn": "string",
        "status": "number",
        "remark": "string",
        "createTime": "string",
        "totalAmount": "number"
      },
      "items": [
        {
          "id": "string",
          "productId": "string",
          "productName": "string",
          "quantity": "number",
          "price": "number"
        }
      ],
      "shipping": {
        "orderId": "string",
        "shippingNumber": "string",
        "shippingMethod": "string"
      },
      "payment": {
        "id": "string",
        "orderId": "string",
        "amount": "number",
        "payStatus": "number"
      }
    }
  }
  ```

### Update Shipping Information
- **URL**: `/api/mall/order/shipping`
- **Method**: `PUT`
- **Description**: Update shipping information for an order
- **Request Body**:
  ```json
  {
    "orderId": "string",
    "shippingNumber": "string",
    "shippingMethod": "string"
  }
  ```

## Payment Management

### Create BTC Payment Invoice
- **URL**: `/api/payments/btc/create-invoice`
- **Method**: `POST`
- **Description**: Create a BTC payment invoice
- **Request Body**:
  ```json
  {
    "orderId": "string",
    "amount": "number"
  }
  ```

### Create Payment Record
- **URL**: `/api/payments/create`
- **Method**: `POST`
- **Description**: Create a payment record
- **Request Body**:
  ```json
  {
    "orderId": "string",
    "amount": "number",
    "payType": "string"
  }
  ```

### Get Payment Status
- **URL**: `/api/payments/status/{orderId}`
- **Method**: `GET`
- **Description**: Get payment status for an order

### Update Payment Status
- **URL**: `/api/payments/update-status`
- **Method**: `POST`
- **Description**: Update payment status
- **Request Body**:
  ```json
  {
    "orderId": "string",
    "payStatus": "number"
  }
  ```

## Cart Management

### Get Cart
- **URL**: `/api/mall/cart`
- **Method**: `GET`
- **Description**: Get current member's cart
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "items": [
        {
          "productId": "number",
          "quantity": "number",
          "price": "number"
        }
      ],
      "total": "number"
    }
  }
  ```

### Update Cart Item
- **URL**: `/api/mall/cart/update`
- **Method**: `POST`
- **Description**: Add or update an item in the cart
- **Request Body**:
  ```json
  {
    "productId": "number",
    "quantity": "number"
  }
  ```

### Remove Cart Item
- **URL**: `/api/mall/cart/remove/{productId}`
- **Method**: `POST`
- **Description**: Remove an item from the cart

## Product Management

### Get Product List
- **URL**: `/api/mall/product2/list`
- **Method**: `GET`
- **Description**: Get product list with optional filtering
- **Query Parameters**:
  - `category`: Product category
  - `name`: Product name
  - `status`: Product status
- **Response**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "id": "number",
        "name": "string",
        "category": "string",
        "price": "number",
        "status": "number"
      }
    ]
  }
  ```

### Get Product Categories
- **URL**: `/api/mall/product2/categories`
- **Method**: `GET`
- **Description**: Get all product categories

### Get Products by Category
- **URL**: `/api/mall/product2/category/{category}`
- **Method**: `GET`
- **Description**: Get products by category

### Get Product Detail
- **URL**: `/api/mall/product2/detail/{id}`
- **Method**: `GET`
- **Description**: Get detailed information about a product

## Coupon Management

### Get Coupon
- **URL**: `/api/mall/coupon/{code}`
- **Method**: `GET`
- **Description**: Get coupon information by code
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "code": "string",
      "status": "number",
      "totalCount": "number",
      "usedCount": "number"
    }
  }
  ```

## System Management

### Get Dictionary Data
- **URL**: `/api/mall/system/dict/{dictType}`
- **Method**: `GET`
- **Description**: Get dictionary data by type 