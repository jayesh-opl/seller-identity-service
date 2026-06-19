package com.marketplace.selleridentity.seller;

import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.seller.dto.CreateSellerRequest;
import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.repository.SellerRepository;
import com.marketplace.selleridentity.seller.service.SellerOnboardingService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerOnboardingServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SellerOnboardingService sellerOnboardingService;

    @Test
    void createSeller_shouldSucceed() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = createUserWithId(userId);
        CreateSellerRequest request = new CreateSellerRequest("My Store");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId(userId)).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenAnswer(invocation -> {
            Seller seller = invocation.getArgument(0);
            setId(seller, UUID.randomUUID());
            return seller;
        });

        CreateSellerResponse response = sellerOnboardingService.createSeller(userId, request);

        assertThat(response.sellerId()).isNotNull();
        assertThat(response.displayName()).isEqualTo("My Store");
        assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void createSeller_shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreateSellerRequest request = new CreateSellerRequest("Shop");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerOnboardingService.createSeller(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void createSeller_shouldThrowWhenSellerAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = createUserWithId(userId);
        CreateSellerRequest request = new CreateSellerRequest("Duplicate Shop");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> sellerOnboardingService.createSeller(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seller profile already exists");
    }

    private User createUserWithId(UUID id) throws Exception {
        User user = User.create("seller@example.com", "hashed", "Test", "User", "+919876543210");
        setId(user, id);
        return user;
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
