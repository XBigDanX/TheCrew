package game.thecrew.mission;

import game.thecrew.model.*;
import game.thecrew.model.taskrules.*;

import java.util.List;

public class MissionLibrary {

    private MissionLibrary() {
    }

    public static Mission forPlayerCount(int missionId, int playerCount) {
        return switch (missionId) {
            case 1 -> getMission1(playerCount);
            case 2 -> getMission2(playerCount);
            case 3 -> getMission3(playerCount);
            case 4 -> getMission4(playerCount);
            case 5 -> getMission5(playerCount);
            case 6 -> getMission6(playerCount);
            case 7 -> getMission7(playerCount);
            case 8 -> getMission8(playerCount);
            case 9 -> getMission9(playerCount);
            case 10 -> getMission10(playerCount);
            case 11 -> getMission11(playerCount);
            case 12 -> getMission12(playerCount);
            case 13 -> getMission13(playerCount);
            case 14 -> getMission14(playerCount);
            case 15 -> getMission15(playerCount);
            case 16 -> getMission16(playerCount);
            case 17 -> getMission17(playerCount);
            case 18 -> getMission18(playerCount);
            case 19 -> getMission19(playerCount);
            case 20 -> getMission20(playerCount);
            case 21 -> getMission21(playerCount);
            case 22 -> getMission22(playerCount);
            case 23 -> getMission23(playerCount);
            case 24 -> getMission24(playerCount);
            case 25 -> getMission25(playerCount);
            case 26 -> getMission26(playerCount);
            case 27 -> getMission27(playerCount);
            case 28 -> getMission28(playerCount);
            case 29 -> getMission29(playerCount);
            case 30 -> getMission30(playerCount);
            case 31 -> getMission31(playerCount);
            case 32 -> getMission32(playerCount);
            default -> throw new IllegalArgumentException("Unknown mission: " + missionId);
        };
    }

    public static Mission getMission1(int playerCount) {
        return new Mission(1, 1, "Signal Reception — Win a trick containing any Yellow card",
                List.of(
                        new Task("Win a Yellow",
                                SimpleTaskRule.winAnyOfColor(CardColor.YELLOW))
                ), playerCount);
    }

    public static Mission getMission2(int playerCount) {
        return new Mission(2, 1, "Locate Beacon — Win the Yellow 1 card",
                List.of(
                        new Task("Win Yellow 1",
                                SimpleTaskRule.winSpecificCard(CardColor.YELLOW, 1))
                ), playerCount);
    }

    public static Mission getMission3(int playerCount) {
        return new Mission(3, 2, "Low Energy Reading — Win a trick with total value ≤ 10",
                List.of(
                        new Task("Win a trick sum lower than 11",
                                SimpleTaskRule.sumLowerThan(11))
                ), playerCount);
    }

    public static Mission getMission4(int playerCount) {
        return new Mission(4, 2, "Dual Frequency — Win Red 5 and Blue 3",
                List.of(
                        new Task("Win Red 5",
                                SimpleTaskRule.winSpecificCard(CardColor.RED, 5)),

                        new Task("Win Blue 3",
                                SimpleTaskRule.winSpecificCard(CardColor.BLUE, 3))
                ), playerCount);
    }

    public static Mission getMission5(int playerCount) {
        return new Mission(5, 2, "High Energy Signal — Win a trick containing a card of value 9",
                List.of(
                        new Task("Win a trick with value 9",
                                SimpleTaskRule.winAnyOfValue(9))
                ), playerCount);
    }

    public static Mission getMission6(int playerCount) {
        return new Mission(6, 2, "Stealth Protocol — Don't win any tricks",
                List.of(
                        new Task("Win no tricks",
                                SimpleTaskRule.trickCount(0))
                ), playerCount);
    }

    public static Mission getMission7(int playerCount) {
        return new Mission(7, 3, "Calibrate Sensors — Win exactly 2 tricks",
                List.of(
                        new Task("Win exactly 2 tricks",
                                SimpleTaskRule.trickCount(2))
                ), playerCount);
    }

    public static Mission getMission8(int playerCount) {
        return new Mission(8, 3, "First Response — Win the first trick of the mission",
                List.of(
                        new Task("Win the first trick",
                                SimpleTaskRule.firstTrick())
                ), playerCount);
    }

    public static Mission getMission9(int playerCount) {
        return new Mission(9, 3, "Last Transmission — Win the last trick of the mission",
                List.of(
                        new Task("Win the last trick",
                                SimpleTaskRule.lastTrick())
                ), playerCount);
    }

    public static Mission getMission10(int playerCount) {
        return new Mission(10, 3, "Spectrum Analysis — Collect at least one card of each color",
                List.of(
                        new Task("Collect all 4 colors",
                                SimpleTaskRule.collectAllColors())
                ), playerCount);
    }

    public static Mission getMission11(int playerCount) {
        return new Mission(11, 3, "Submarine Scan — Collect 1 submarine card",
                List.of(
                        new Task("Collect 1 submarine",
                                SimpleTaskRule.collectCount(null, 1))
                ), playerCount);
    }

    public static Mission getMission12(int playerCount) {
        return new Mission(12, 4, "Red Alert — Win Red 9 and collect at least 3 Red cards",
                List.of(
                        new Task("Win Red 9",
                                SimpleTaskRule.winSpecificCard(CardColor.RED, 9)),

                        new Task("Collect 3 Red cards",
                                SimpleTaskRule.collectCount(CardColor.RED, 3))
                ), playerCount);
    }

    public static Mission getMission13(int playerCount) {
        return new Mission(13, 4, "Green Harvest — Collect 4 Green cards and win a high-sum trick",
                List.of(
                        new Task("Collect 4 Green cards",
                                SimpleTaskRule.collectCount(CardColor.GREEN, 4)),

                        new Task("Win a trick all > 5",
                                SimpleTaskRule.allInRange(6, 9))
                ), playerCount);
    }

    public static Mission getMission14(int playerCount) {
        return new Mission(14, 4, "Blue Streak — Win a trick all less than 6 and collect Blue cards",
                List.of(
                        new Task("Win a trick all < 6",
                                SimpleTaskRule.allInRange(1, 5)),

                        new Task("Collect at least 1 Blue card",
                                SimpleTaskRule.collectCount(CardColor.BLUE, 1))
                ), playerCount);
    }

    public static Mission getMission15(int playerCount) {
        return new Mission(15, 4, "Odd Resonance — Win a trick with all odd card values",
                List.of(
                        new Task("Win an all-odd trick",
                                SimpleTaskRule.allOdd())
                ), playerCount);
    }

    public static Mission getMission16(int playerCount) {
        return new Mission(16, 5, "Color Rivalry — Collect more Yellow cards than Blue cards",
                List.of(
                        new Task("More Yellow than Blue",
                                SimpleTaskRule.colorComparison(CardColor.YELLOW, CardColor.BLUE))
                ), playerCount);
    }

    public static Mission getMission17(int playerCount) {
        return new Mission(17, 5, "Submarine Patrol — Collect 3 submarine cards",
                List.of(
                        new Task("Collect 3 submarines",
                                SimpleTaskRule.collectCount(null, 3))
                ), playerCount);
    }

    public static Mission getMission18(int playerCount) {
        return new Mission(18, 5, "Full Spectrum — Collect at least 2 cards of each color",
                List.of(
                        new Task("Collect 2 Yellow", SimpleTaskRule.collectCount(CardColor.YELLOW, 2)),
                        new Task("Collect 2 Green", SimpleTaskRule.collectCount(CardColor.GREEN, 2)),
                        new Task("Collect 2 Blue", SimpleTaskRule.collectCount(CardColor.BLUE, 2)),
                        new Task("Collect 2 Red", SimpleTaskRule.collectCount(CardColor.RED, 2))
                ), playerCount);
    }

    public static Mission getMission19(int playerCount) {
        return new Mission(19, 5, "Predictor — Predict and win exactly 3 tricks",
                List.of(
                        new Task("Predict exactly 3 tricks",
                                SimpleTaskRule.trickCount(3))
                ), playerCount);
    }

    public static Mission getMission20(int playerCount) {
        return new Mission(20, 5, "Chain Reaction — Win at least 2 tricks in a row",
                List.of(
                        new Task("Win 2 consecutive tricks",
                                SimpleTaskRule.consecutiveTricks(2))
                ), playerCount);
    }

    public static Mission getMission21(int playerCount) {
        return new Mission(21, 6, "Captain's Shadow — Win fewer tricks than the captain",
                List.of(
                        new Task("Fewer tricks than captain",
                                SimpleTaskRule.fewerThanCaptain())
                ), playerCount);
    }

    public static Mission getMission22(int playerCount) {
        return new Mission(22, 6, "Yellow Empire — Collect all 9 Yellow cards",
                List.of(
                        new Task("Collect all Yellow cards",
                                SimpleTaskRule.entireColor())
                ), playerCount);
    }

    public static Mission getMission23(int playerCount) {
        return new Mission(23, 6, "Split Focus — Don't win any tricks but win Green 6",
                List.of(
                        new Task("Win no tricks",
                                SimpleTaskRule.trickCount(0)),

                        new Task("Win Green 6",
                                SimpleTaskRule.winSpecificCard(CardColor.GREEN, 6))
                ), playerCount);
    }

    public static Mission getMission24(int playerCount) {
        return new Mission(24, 6, "Bookends — Win both the first and the last trick",
                List.of(
                        new Task("Win first and last trick",
                                SimpleTaskRule.firstAndLast())
                ), playerCount);
    }

    public static Mission getMission25(int playerCount) {
        return new Mission(25, 7, "Safe Passage — Don't lead with Yellow and win Blue 1",
                List.of(
                        new Task("Don't lead Yellow",
                                SimpleTaskRule.avoidLead(CardColor.YELLOW)),

                        new Task("Win Blue 1",
                                SimpleTaskRule.winSpecificCard(CardColor.BLUE, 1))
                ), playerCount);
    }

    public static Mission getMission26(int playerCount) {
        return new Mission(26, 7, "High Voltage — Win a trick with a high total sum",
                List.of(
                        new Task("Win a trick with sum > threshold",
                                SimpleTaskRule.sumGreaterThan(15, 20, 25))
                ), playerCount);
    }

    public static Mission getMission27(int playerCount) {
        return new Mission(27, 7, "Color Balance — Collect same number of Blue and Yellow (at least 1 each)",
                List.of(
                        new Task("Equal Blue and Yellow",
                                SimpleTaskRule.colorEqual(CardColor.BLUE, CardColor.YELLOW))
                ), playerCount);
    }

    public static Mission getMission28(int playerCount) {
        return new Mission(28, 7, "Submarine Fleet — Collect 5 submarine cards",
                List.of(
                        new Task("Collect 5 submarines",
                                SimpleTaskRule.collectCount(null, 5))
                ), playerCount);
    }

    public static Mission getMission29(int playerCount) {
        return new Mission(29, 8, "Triple Threat — Predict 2 tricks, win Green 9, win last trick with Blue 3",
                List.of(
                        new Task("Predict exactly 2 tricks",
                                SimpleTaskRule.trickCount(2)),

                        new Task("Win Green 9",
                                SimpleTaskRule.winSpecificCard(CardColor.GREEN, 9)),

                        new Task("Win last trick with Blue 3",
                                SimpleTaskRule.lastTrickWithCard(CardColor.BLUE, 3))
                ), playerCount);
    }

    public static Mission getMission30(int playerCount) {
        return new Mission(30, 8, "Even Handed — Win a trick with all even values and avoid leading Red",
                List.of(
                        new Task("Win an all-even trick",
                                SimpleTaskRule.allInRange(2, 8)), // Simplification for parity check in range

                        new Task("Don't lead Red",
                                SimpleTaskRule.avoidLead(CardColor.RED))
                ), playerCount);
    }

    public static Mission getMission31(int playerCount) {
        return new Mission(31, 9, "Planet Nine — Collect all Green cards and win exactly 1 trick",
                List.of(
                        new Task("Collect all Green cards",
                                SimpleTaskRule.entireColor()),

                        new Task("Win exactly 1 trick",
                                SimpleTaskRule.trickCount(1))
                ), playerCount);
    }

    public static Mission getMission32(int playerCount) {
        return new Mission(32, 10, "The Final Frontier — Collect all 4 colors, outshine everyone, win last trick with Yellow 1",
                List.of(
                        new Task("Collect all 4 colors",
                                SimpleTaskRule.collectAllColors()),

                        new Task("Win more tricks than everyone",
                                SimpleTaskRule.moreThanEveryone()),

                        new Task("Win last trick with Yellow 1",
                                SimpleTaskRule.lastTrickWithCard(CardColor.YELLOW, 1))
                ), playerCount);
    }

}



