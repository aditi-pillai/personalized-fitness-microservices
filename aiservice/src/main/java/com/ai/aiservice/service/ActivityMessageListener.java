package com.ai.aiservice.service;

import com.ai.aiservice.model.Activity;
import com.ai.aiservice.model.Recommendation;
import com.ai.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processActivity(Activity activity){
        log.info("received activity for processing: {}", activity.getId());
//        log.info("generated recommendation for activity: {}", aiService.generateRecommendations(activity));
        Recommendation recommendation = aiService.generateRecommendations(activity);
        recommendationRepository.save(recommendation);
    }
}

