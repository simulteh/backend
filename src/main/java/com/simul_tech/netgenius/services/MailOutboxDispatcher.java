package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.repositories.MailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailOutboxDispatcher {
    private final MailOutboxService service;
    private final MailOutboxRepository outbox;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void queued(MailOutboxService.Queued event) {
        deliver(event.id());
    }

    @Scheduled(fixedDelayString = "${app.mail.outbox.retry-delay-ms:60000}",
            initialDelayString = "${app.mail.outbox.retry-delay-ms:60000}")
    public void retryPending() {
        for (Long id : outbox.findDueIds(Instant.now(), PageRequest.of(0, 20))) deliver(id);
    }

    private void deliver(Long id) {
        try {
            service.deliver(id);
        } catch (RuntimeException e) {
            // После commit письмо уже сохранено; ошибка воркера не меняет публичный ответ.
            log.warn("Mail outbox {} delivery deferred ({})", id, e.getClass().getSimpleName());
        }
    }
}
