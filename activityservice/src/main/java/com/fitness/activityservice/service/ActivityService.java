package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private ActivityResponse mapToResponse(Activity activity){
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setActivityType(activity.getActivityType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurnt(activity.getCaloriesBurnt());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());

        return response;
    }

    public ActivityResponse trackActivity(ActivityRequest activityRequest) {

        boolean isValidUser = userValidationService.validateUserId(activityRequest.getUserId());

        if (!isValidUser) {
            throw new RuntimeException("Invalid User ID" + activityRequest.getUserId());
        }

        Activity activity = new Activity().builder()
                .userId(activityRequest.getUserId())
                .startTime(activityRequest.getStartTime())
                .activityType(activityRequest.getActivityType())
                .duration(activityRequest.getDuration())
                .caloriesBurnt(activityRequest.getCaloriesBurnt())
                .additionalMetrics(activityRequest.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);


        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        } catch (Exception e) {
            log.error("error in publishing activity to rabbitmq");
            throw new RuntimeException(e);
        }

        return mapToResponse(savedActivity);
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities= activityRepository.findByUserId(userId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivities(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(()->new RuntimeException("Activity not found with id"+activityId));
    }
}
