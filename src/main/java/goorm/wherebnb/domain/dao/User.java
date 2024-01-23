package goorm.wherebnb.domain.dao;

import goorm.wherebnb.config.auth.user.oauth2.OAuth2UserInfo;
import goorm.wherebnb.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String picture;

    private String role;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String phoneNumber;

    private String emergencyNumber;

    private String explanation;

    @Setter
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Embedded
    private Address address;

    @ElementCollection
    private List<Long> wishList;

    @OneToMany(mappedBy = "host")
    private List<Property> properties = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Payment> payments = new ArrayList<>();

    @OneToMany( mappedBy = "user")
//    @JoinColumn(name = "conversation_id")
    private List<UserChat> userChats = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private List<Message> sentMessages;

    @OneToMany(mappedBy = "recipient")
    private List<Message> receivedMessages;

    @Builder
    public User(String name, String email, String picture, String role, String password, String phoneNumber,
                String explanation, String refreshToken, String emergencyNumber, AuthProvider provider, String providerId, Address address, List<Long> wishList) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.emergencyNumber = emergencyNumber;
        this.explanation = explanation;
        this.refreshToken = refreshToken;
        this.provider = provider;
        this.providerId = providerId;
        this.address = address;
        this.wishList = wishList;
    }

    //== 연관관계 메서드 ==//
    public void addProperty(Property property) {
        this.properties.add(property);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }

    public void addPayment(Payment payment) {
        this.payments.add(payment);
        payment.setUser(this);
    }

    public void setUserChats(UserChat userChat) {
        this.userChats.add(userChat);
        userChat.setUser(this);
    }

    public void addSentMessage(Message message) {
        sentMessages.add(message);
        if (message.getSender() != this) {
            message.setSender(this);
        }
    }

    public void addReceivedMessage(Message message) {
        receivedMessages.add(message);
        if (message.getRecipient() != this) {
            message.setRecipient(this);
        }
    }
    public static User createNewUser(AuthProvider provider, String providerId, String name, String email, String imageUrl, String role) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .email(email)
                .picture(imageUrl)
                .role(role)
                .build();
    }

    public void updateUserWithOAuth2UserInfo(OAuth2UserInfo oAuth2UserInfo) {
        this.name = oAuth2UserInfo.getName();
        this.picture = oAuth2UserInfo.getImageUrl();
    }
}
