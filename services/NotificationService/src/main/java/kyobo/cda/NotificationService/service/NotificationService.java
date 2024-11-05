package kyobo.cda.NotificationService.service;

import kyobo.cda.NotificationService.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${aws.ses.sender}")
    private String sender;

    private final SesClient sesClient;
    private final SpringTemplateEngine springTemplateEngine;

    @Async
    public CompletableFuture<String> sendReservationConfirmEmail(ReservationRequestDto reservationRequestDto) {
        String subject = "예약 확정";
        String templateName = "reservation-confirmation.html";
        return sendEmail(reservationRequestDto, subject, templateName);
    }

    @Async
    public CompletableFuture<String> sendReservationCancelEmail(ReservationRequestDto reservationRequestDto) {
        String subject = "예약 취소";
        String templateName = "reservation-cancel.html";
        return sendEmail(reservationRequestDto, subject, templateName);
    }

    private CompletableFuture<String> sendEmail(ReservationRequestDto reservationRequestDto, String subject, String templateName) {
        Context context = new Context();
        context.setVariable("email", reservationRequestDto.getUserEmail());
        context.setVariable("restaurantName", reservationRequestDto.getRestaurantName());
        context.setVariable("reservationDateTime", reservationRequestDto.getReservationDateTime().toString());
        context.setVariable("reservationId", reservationRequestDto.getReservationId().toString());
        context.setVariable("numberOfGuests", reservationRequestDto.getNumberOfGuests());

        String emailContent = springTemplateEngine.process(templateName, context);

        // 이메일 전송
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(sender)
                    .destination(Destination.builder().toAddresses(reservationRequestDto.getUserEmail()).build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(subject)
                                    .charset("UTF-8")
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(emailContent)
                                            .charset("UTF-8")
                                            .build())
                                    .build())
                            .build())
                    .build();
            sesClient.sendEmail(emailRequest);
            log.info("이메일 전송 완료: [발신자] {} -> [수신자] {}", sender, reservationRequestDto.getUserEmail());
        } catch (SesException e) {
            log.error("이메일 전송 실패: {}", e.awsErrorDetails().errorMessage());
            throw e;
        }

        return CompletableFuture.completedFuture("예약 이메일 전송 완료");
    }
}
