package com.marketplace.selleridentity.seller;

import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.entity.SellerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerEntityTest {

    @Test
    void approve_shouldTransitionFromPendingToActive() {
        Seller seller = createSeller();

        seller.approve();

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void reject_shouldTransitionFromPendingToRejected() {
        Seller seller = createSeller();

        seller.reject();

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.REJECTED);
    }

    @Test
    void suspend_shouldTransitionFromActiveToSuspended() {
        Seller seller = createSeller();
        seller.approve(); // PENDING_APPROVAL -> ACTIVE

        seller.suspend();

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.SUSPENDED);
    }

    @Test
    void approve_shouldFailFromActive() {
        Seller seller = createSeller();
        seller.approve();

        assertThatThrownBy(seller::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void approve_shouldFailFromRejected() {
        Seller seller = createSeller();
        seller.reject();

        assertThatThrownBy(seller::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void approve_shouldFailFromSuspended() {
        Seller seller = createSeller();
        seller.approve();
        seller.suspend();

        assertThatThrownBy(seller::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void reject_shouldFailFromActive() {
        Seller seller = createSeller();
        seller.approve();

        assertThatThrownBy(seller::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void suspend_shouldFailFromPending() {
        Seller seller = createSeller();

        assertThatThrownBy(seller::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void suspend_shouldFailFromRejected() {
        Seller seller = createSeller();
        seller.reject();

        assertThatThrownBy(seller::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    @Test
    void suspend_shouldFailFromSuspended() {
        Seller seller = createSeller();
        seller.approve();
        seller.suspend();

        assertThatThrownBy(seller::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid seller status transition");
    }

    private Seller createSeller() {
        User user = User.create("seller@example.com", "hash", "Test", "User", "+919876543210");
        return Seller.create(user, "Test Store");
    }
}
