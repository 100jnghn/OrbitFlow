package com.finalproj.orbitflow.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 알림 전달 전용 서비스
 *
 * @author : 종훈
 * @filename : NotificationService
 * @since : 2026-01-02 오후 4:13 금요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationDeliveryService {

    // ConcurrentHashMap을 사용해 다중 요청 환경에서도 안전하게 관리
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결 생성
     * - 사용자별로 여러 emitter 관리
     * - 연결 종료 시 해당 emitter만 제거
     * - 최초 연결 시 더미 이벤트 전송 (연결 안정화)
     */
    public SseEmitter createEmitter(Long employeeId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간

        // 사용자 emitter SET 생성 or 조회
        emitters.computeIfAbsent(employeeId, id -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        // 연결 종료 시 해당 emitter 제거
        emitter.onCompletion(() -> removeEmitter(employeeId, emitter));
        emitter.onTimeout(() -> removeEmitter(employeeId, emitter));
        emitter.onError(e -> removeEmitter(employeeId, emitter));

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     * Redis Subscriber에서 호출
     * NotificationDeliveryService가 SSE로 밀어준다
     */
    public void send(Long employeeId, Object data) {

        Set<SseEmitter> userEmitters = emitters.get(employeeId);
        log.info("SSE send 호출됨 employeeId={}", employeeId);

        if (userEmitters == null) {
            log.warn("SSE emitter 없음 employeeId={}", employeeId);
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (Exception e) {
                removeEmitter(employeeId, emitter);
            }
        }
    }

    // 해당 emitter 제거
    private void removeEmitter(Long employeeId, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(employeeId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(employeeId);
            }
        }
    }

}
