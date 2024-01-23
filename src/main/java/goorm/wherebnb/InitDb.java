package goorm.wherebnb;


import goorm.wherebnb.domain.dao.*;
import goorm.wherebnb.domain.dto.request.BookingRequest;
import goorm.wherebnb.domain.dto.request.PaymentRequest;
import goorm.wherebnb.repository.PropertyRepository;
import goorm.wherebnb.service.BookingService;
import goorm.wherebnb.service.PropertyService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() throws IOException {
        initService.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final PropertyService propertyService;
        private final PropertyRepository propertyRepository;
        private final BookingService bookingService;
        private final BCryptPasswordEncoder passwordEncoder;

        public void dbInit() throws IOException {
            User user = User.builder()
                    .name("John Doe")
                    .email("johndoe@example.com")
                    .picture("profilePic.jpg")
                    .password(passwordEncoder.encode("password123"))
                    .role("USER")
                    .phoneNumber("1234567890")
                    .emergencyNumber("0987654321")
                    .explanation("안녕 나는 조 도")
                    .address(new Address("Country", "State", "City", "Street", "Details", "Zipcode", 10.0, 20.0))
                    .build();

            User sampleUser = User.builder()
                    .name("홍길동")
                    .email("honggildong@example.com")
                    .picture("profilePic.jpg")
                    .password(passwordEncoder.encode("password123"))
                    .phoneNumber("01012345678")
                    .emergencyNumber("01087654321")
                    .explanation("안녕 나는 홍길동")
                    .address(new Address("대한민국", "서울", "강남구", "도곡동", "세부주소", "12345", 37.4979, 127.0276))
                    .build();

            Address propertyAddress = Address.builder()
                    .country("Country")
                    .state("State")
                    .city("City")
                    .street("Street")
                    .details("Apartment 101")
                    .zipcode("123456")
                    .latitude(10.0)
                    .longitude(20.0)
                    .build();

            PropertyDetail propertyDetail = PropertyDetail.builder()
                    .maxPeople(4)
                    .selfCheckIn(true)
                    .petAvailable(false)
                    .smokeAvailable(false)
                    .checkInTime(14)
                    .checkOutTime(11)
                    .bedroom(2)
                    .bed(3)
                    .bathroom(1)
                    .build();

            Property property = Property.builder()
                    .host(user)
                    .propertyName("Lovely Apartment")
                    .propertyType(PropertyType.호텔)
                    .propertyExplanation("A cozy and modern apartment in the city center.")
                    .propertyDetail(propertyDetail)
                    .address(propertyAddress)
                    .price(100)
                    .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                    .amenities(Arrays.asList("WiFi", "TV", "Air Conditioning"))
                    .build();

            Review review1 = Review.builder()
                    .user(sampleUser)
                    .property(property)
                    .score(Score.builder()
                            .cleanScore(4)
                            .communicationScore(4)
                            .checkInScore(5)
                            .accuracyScore(5)
                            .locationScore(4)
                            .priceScore(3)
                            .build())
                    .content("정말 깨끗하고 아늑한 숙소였습니다. 호스트의 친절함도 좋았어요.")
                    .build();

            Review review2 = Review.builder()
                    .user(sampleUser)
                    .property(property)
                    .score(Score.builder()
                            .cleanScore(3)
                            .communicationScore(2)
                            .checkInScore(4)
                            .accuracyScore(2)
                            .locationScore(4)
                            .priceScore(5)
                            .build())
                    .content("위치가 매우 좋았지만, 체크인 과정이 조금 복잡했어요.")
                    .build();

            Review review3 = Review.builder()
                    .user(sampleUser)
                    .property(property)
                    .score(Score.builder()
                            .cleanScore(4)
                            .communicationScore(3)
                            .checkInScore(2)
                            .accuracyScore(5)
                            .locationScore(4)
                            .priceScore(5)
                            .build())
                    .content("완벽한 숙소! 모든 것이 기대 이상이었습니다.")
                    .build();

            em.persist(user);
            em.persist(sampleUser);
            em.persist(property);
            em.persist(review1);
            em.persist(review2);
            em.persist(review3);

            bookingService.createBooking(1L,
                    BookingRequest.builder()
                            .userId(1L)
                            .checkInDate(LocalDate.of(2024, 2, 21))
                            .checkOutDate(LocalDate.of(2024, 2, 24))
                            .numberOfAdults(2)
                            .numberOfChildren(1)
                            .numberOfInfants(1)
                            .paymentRequest(PaymentRequest.builder()
                                    .email(sampleUser.getEmail())
                                    .paymentMethod(PaymentMethod.CARD)
                                    .cardNumber("1234-1234-1234-1234")
                                    .expirationMonth("02")
                                    .expirationYear("28")
                                    .cvc(123)
                                    .totalPrice(1000000)
                                    .nation("KOREA")
                                    .build())
                            .message("hello")
                            .build()
            );

            bookingService.createBooking(1L,
                    BookingRequest.builder()
                            .userId(2L)
                            .checkInDate(LocalDate.of(2024, 2, 25))
                            .checkOutDate(LocalDate.of(2024, 2, 27))
                            .numberOfAdults(2)
                            .numberOfChildren(1)
                            .numberOfInfants(1)
                            .paymentRequest(PaymentRequest.builder()
                                    .email(sampleUser.getEmail())
                                    .paymentMethod(PaymentMethod.CARD)
                                    .cardNumber("1234-1234-1234-1234")
                                    .expirationMonth("02")
                                    .expirationYear("28")
                                    .cvc(123)
                                    .totalPrice(120000)
                                    .nation("KOREA")
                                    .build())
                            .message("hello")
                            .build()
            );

            bookingService.createBooking(1L,
                    BookingRequest.builder()
                            .userId(2L)
                            .checkInDate(LocalDate.of(2024, 2, 2))
                            .checkOutDate(LocalDate.of(2024, 2, 10))
                            .numberOfAdults(1)
                            .numberOfChildren(1)
                            .numberOfInfants(1)
                            .paymentRequest(PaymentRequest.builder()
                                    .email(sampleUser.getEmail())
                                    .paymentMethod(PaymentMethod.CARD)
                                    .cardNumber("1234-1234-1234-1234")
                                    .expirationMonth("02")
                                    .expirationYear("28")
                                    .cvc(123)
                                    .totalPrice(900000)
                                    .nation("KOREA")
                                    .build())
                            .message("hello")
                            .build()
            );

        }


    }
}
