package com.secondhand.backend.model;

/**
 * Life cycle of an advertisement:
 * PENDING  -> waiting for admin review
 * ACTIVE   -> approved and visible to everyone
 * REJECTED -> rejected by admin
 * DELETED  -> logically deleted (kept in database)
 * SOLD     -> marked as sold by the owner
 */
public enum AdStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    DELETED,
    SOLD
}
