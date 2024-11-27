package kyobo.cda.NotificationService.service;

import kyobo.cda.NotificationService.dto.ReservationRequestDto;
import kyobo.cda.NotificationService.dto.WaitListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${aws.ses.sender}")
    private String sender;

    private final SesClient sesClient;
    private final SpringTemplateEngine springTemplateEngine;

    /**
     * 예약 확정 이메일 전송 메서드이다.
     *
     * @param reservationRequestDto 예약 확정 정보
     * @return CompletableFuture<String> 이메일 전송 결과
     */
    @Async
    public CompletableFuture<String> sendReservationConfirmEmail(ReservationRequestDto reservationRequestDto) {
        String subject = "예약 확정";
        String templateName = "reservation-confirm.html";

        Context context = new Context();
        context.setVariable("email", reservationRequestDto.getUserEmail());
        context.setVariable("restaurantName", reservationRequestDto.getRestaurantName());
        context.setVariable("reservationDateTime", reservationRequestDto.getReservationDateTime().toString());
        context.setVariable("reservationId", reservationRequestDto.getReservationId().toString());
        context.setVariable("numberOfGuests", reservationRequestDto.getNumberOfGuests());

        return sendEmail(context, subject, templateName);
    }

    /**
     * 예약 취소 이메일 전송 메서드이다.
     *
     * @param reservationRequestDto 예약 취소 정보
     * @return CompletableFuture<String> 이메일 전송 결과
     */
    @Async
    public CompletableFuture<String> sendReservationCancelEmail(ReservationRequestDto reservationRequestDto) {
        String subject = "예약 취소";
        String templateName = "reservation-cancel.html";

        Context context = new Context();
        context.setVariable("email", reservationRequestDto.getUserEmail());
        context.setVariable("restaurantName", reservationRequestDto.getRestaurantName());
        context.setVariable("reservationDateTime", reservationRequestDto.getReservationDateTime().toString());
        context.setVariable("numberOfGuests", reservationRequestDto.getNumberOfGuests());

        return sendEmail(context, subject, templateName);
    }

    /**
     * 예약 대기자들에게 알림을 전송하는 메서드이다.
     *
     * @param waitListDtoList 예약 대기자 정보 리스트
     * @return CompletableFuture<String> 이메일 전송 결과
     */
    @Async
    public CompletableFuture<String> sendWaitingReservationEmail(List<WaitListDto> waitListDtoList) {
        String subject = "예약 대기";
        String templateName = "reservation-waiting.html";

        Context context = new Context();
        for (WaitListDto waitListDto : waitListDtoList) {
            context.setVariable("email", waitListDto.getUserEmail());
            context.setVariable("restaurantName", waitListDto.getRestaurantName());
            context.setVariable("reservationDateTime", waitListDto.getReservationDateTime().toString());
            sendEmail(context, subject, templateName);
        }
        return CompletableFuture.completedFuture("예약 대기자들에게 알림 전송 완료");
    }

    /**
     * 이메일을 전송하는 메서드이다.
     *
     * @param context 이메일 템플릿에 전달할 데이터
     * @param subject 이메일 제목
     * @param templateName 이메일 템플릿 이름
     * @return CompletableFuture<String> 이메일 전송 결과
     */
    private CompletableFuture<String> sendEmail(Context context, String subject, String templateName) {
        String userEmail = context.getVariable("email").toString();
        String emailContent = springTemplateEngine.process(templateName, context);

        // 이메일 전송
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(sender)
                    .destination(Destination.builder().toAddresses(userEmail).build())
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
            log.info("이메일 전송 완료: [발신자] {} -> [수신자] {}", sender, userEmail);
        } catch (SesException e) {
            log.error("이메일 전송 실패: {}", e.awsErrorDetails().errorMessage());
            throw e;
        }

        return CompletableFuture.completedFuture("예약 이메일 전송 완료");
    }
}
