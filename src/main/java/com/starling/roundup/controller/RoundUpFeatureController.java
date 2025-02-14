package com.starling.roundup.controller;

import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import com.starling.roundup.service.RoundUpFeatureService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/starling")
public class RoundUpFeatureController {

    private final RoundUpFeatureService roundUpFeatureService;

    public RoundUpFeatureController(RoundUpFeatureService roundUpFeatureService) {
        this.roundUpFeatureService = roundUpFeatureService;
    }

    @GetMapping("/account/{accountUid}/category/{categoryUid}/round-up")
    public Mono<Amount> roundUpTransactions(@RequestHeader("Authorization") String authorizationHeader, @RequestParam("changesSince") String changesSince, @PathVariable("accountUid") String accountUid, @PathVariable("categoryUid") String categoryUid) {
       return roundUpFeatureService.processWeeklyRoundUp(accountUid, categoryUid, changesSince, authorizationHeader);
    }

    @PutMapping("/account/{accountUid}/savings-goal/{savingsGoalUid}/transfer-round-up")
    public Mono<Void> transferRoundUpToSavings(@RequestHeader("Authorization") String authorizationHeader,
                                               @PathVariable("accountUid") String accountUid,
                                               @PathVariable("savingsGoalUid") String savingsGoalUid, @RequestBody AddMoneyRequest addMoneyRequest) {
        return roundUpFeatureService.transferToSavings(accountUid, savingsGoalUid, addMoneyRequest, authorizationHeader);
    }

}
