package com.marketplace.selleridentity.seller;

import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.entity.SellerStatus;
import com.marketplace.selleridentity.seller.repository.SellerRepository;
import com.marketplace.selleridentity.seller.service.SellerLifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerLifecycleServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerLifecycleService sellerLifecycleService;

    @Test
    void getCurrentSeller_shouldReturnSellerResponse() {
        UUID userId = UUID.randomUUID();
        Seller seller = createSellerWithId(userId);

        when(sellerRepository.findByUserId(userId)).thenReturn(Optional.of(seller));

        CreateSellerResponse response = sellerLifecycleService.getCurrentSeller(userId);

        assertThat(response.sellerId()).isNotNull();
        assertThat(response.displayName()).isEqualTo("Test Store");
        assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void getCurrentSeller_shouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(sellerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerLifecycleService.getCurrentSeller(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seller profile not found");
    }

    @Test
    void approveSeller_shouldTransitionToActive() {
        UUID sellerId = UUID.randomUUID();
        Seller seller = createSellerWithId(sellerId);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        sellerLifecycleService.approveSeller(sellerId);

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void rejectSeller_shouldTransitionToRejected() {
        UUID sellerId = UUID.randomUUID();
        Seller seller = createSellerWithId(sellerId);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        sellerLifecycleService.rejectSeller(sellerId);

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.REJECTED);
    }

    @Test
    void suspendSeller_shouldTransitionToSuspended() {
        UUID sellerId = UUID.randomUUID();
        Seller seller = createSellerWithId(sellerId);
        seller.approve(); // must be ACTIVE to suspend

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        sellerLifecycleService.suspendSeller(sellerId);

        assertThat(seller.getStatus()).isEqualTo(SellerStatus.SUSPENDED);
    }

    @Test
    void approveSeller_shouldThrowWhenNotFound() {
        UUID sellerId = UUID.randomUUID();
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerLifecycleService.approveSeller(sellerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seller not found");
    }

    private Seller createSellerWithId(UUID id) {
        User user = User.create("seller@example.com", "hash", "Test", "User", "+919876543210");
        Seller seller = Seller.create(user, "Test Store");
        setId(seller, id);
        return seller;
    }

    private void setId(Object entity, UUID id) {
        try {
            Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
