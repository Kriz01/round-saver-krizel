# Roundup Feature

## Overview
The Roundup Feature allows users to round up their account transactions to the nearest whole amount, and transfer the rounded-up amounts to a designated savings goal. The feature fetches transactions, calculates the round-up amounts, and provides methods to process and transfer the funds.

## Features
1. **Round-up Transactions**: Automatically rounds up eligible transactions (those with direction "OUT", not marked "INTERNAL_TRANSFER", and with status "SETTLED") and computes the round-up amount.
2. **Transfer to Savings**: After calculating the round-up amount, the system can transfer the rounded amount to a designated savings goal.
3. **Currency Handling**: The feature processes transactions in various currencies and ensures proper handling of currency codes and minor units.  
   **Note:** The feature currently does **not** support multiple currencies in the same feed. If the feed contains transactions in more than one currency, it will throw an error. The system expects only a single currency per feed for proper processing. In case of multiple currencies, the system throws an exception.
4. **Integration with Starling API**: The feature interacts with Starling's API to fetch transaction data and transfer money to savings goals.

---

## Requirements

- **Java 21+**
- **Reactive Programming** (using `Mono` from Project Reactor)
- **Maven** as a build tool
- A working **Starling API** (for API interactions)

## Setup Instructions

### Clone the Repository
```bash
git clone https://github.com/your-repo/roundup-feature.git
cd roundup-feature
```
### Build the Project
```bash
mvn clean install
```

## Starling API Integration

The **Roundup Feature** makes use of several endpoints provided by the **Starling API** to interact with user accounts, fetch transactions, and manage savings goals. Below are the key Starling API endpoints used by this feature:

### 1. **Accounts API**
- This endpoint allows you to retrieve a list of accounts linked to the authenticated user.
  
#### Example Request:
```http
GET /api/v2/accounts
Authorization: Bearer your-auth-token
```
### 2. **Feed API**
- This endpoint fetches transaction feed data for a specific account and category. It supports a changesSince query parameter to filter transactions after a given timestamp.

#### Example Request:
```http
GET /api/v2/feed/account/48f0b7f2-af99-4041-8f3f-f33580fdccf3/category/48f00630-62f0-4e42-8e6a-0a7523a64c3f?changesSince={changesSince}
Authorization: Bearer your-auth-token
```
### 3. **Create Savings Goal API**
- This endpoint allows you to create a new savings goal within a specific account. You can specify details such as the goal name and target amount.
  
 #### Example Request:
```http
PUT  /api/v2/account/{accountUid}/savings-goals
Authorization: Bearer your-auth-token
``` 
## Roundup Feature Endpoints

This document provides an overview of the two key API endpoints used to process the Round-up feature: **Round Up Transactions** and **Transfer Round-up to Savings**. 

---

### 1. Round Up Transactions

This endpoint processes the round-up for an account. It fetches feed transactions, calculates the round-up amounts, and returns the total amount rounded up.

#### Endpoint
`GET /api/starling/account/{accountUid}/category/{categoryUid}/round-up`

#### Parameters:
- **Authorization** (header): Bearer token for API authentication
- **changesSince** (query parameter): The timestamp for fetching transactions since that time (in ISO 8601 format)
- **accountUid** (path parameter): The unique identifier for the account
- **categoryUid** (path parameter): The unique identifier for the category

#### Response:
Returns a JSON object with the total round-up amount in the selected currency.
```
{
  "currency": "GBP",
  "amount": 3.50
}
```

### 2. Transfer Round-up to Savings

This endpoint transfers the calculated round-up amount to a savings goal for the given account.

#### Endpoint
`GET /api/starling/account/{accountUid}/savings-goal/{savingsGoalUid}/transfer-round-up`

#### Parameters:
- **Authorization** (header): Bearer token for API authentication
- **savingsGoalUid** (path parameter): The unique identifier for the savings goal
- **accountUid** (path parameter): The unique identifier for the account
- **AddMoneyRequest** (body): Contains the amount to be transferred and other relevant information in the request body

#### Response:
Return 200 response





