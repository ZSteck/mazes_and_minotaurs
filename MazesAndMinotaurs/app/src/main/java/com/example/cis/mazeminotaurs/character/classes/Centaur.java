package com.example.cis.mazeminotaurs.character.classes;

import com.example.cis.mazeminotaurs.AttributeScore;
import com.example.cis.mazeminotaurs.Equipment;
import com.example.cis.mazeminotaurs.EquipmentDB;
import com.example.cis.mazeminotaurs.R;
import com.example.cis.mazeminotaurs.Weapon;
import com.example.cis.mazeminotaurs.character.Gender;
import com.example.cis.mazeminotaurs.character.PlayerCharacter;
import com.example.cis.mazeminotaurs.character.stats.Score;
import com.example.cis.mazeminotaurs.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by jusmith on 5/15/17.
 */

public class Centaur extends Warrior implements Level{
    private ArrayList<HashMap<Score, Integer>> mScoreLevelChoice = new ArrayList<>();

    public Centaur(PlayerCharacter playerCharacter, Weapon weaponOfChoice, Weapon startingWeapon) {
        Score[] primAttrs = {Score.MIGHT, Score.SKILL};
        ArrayList<Score> primAttributes = new ArrayList<>();
        Collections.addAll(primAttributes, primAttrs);

        // Setting up for equipment check
        EquipmentDB equipmentDB = EquipmentDB.getInstance();
        ArrayList<Weapon> possibleWepsOfChoice = new ArrayList<>();
        ArrayList<Weapon> possibleStartWeps = new ArrayList<>();
        ArrayList<Equipment> startGear = new ArrayList<>();

        for (int choiceId: new int[] {R.string.bow, R.string.javelin, R.string.club, R.string.spear}){
            possibleWepsOfChoice.add(equipmentDB.getWeapon(choiceId));
        }

        for (int startId: Util.sMissleWeapons) {
            possibleStartWeps.add(equipmentDB.getWeapon(startId));
        }

        // Set up weapon of choice
        if (possibleWepsOfChoice.contains(weaponOfChoice)) {
            setWeaponOfChoice(weaponOfChoice);
        } else {
            setWeaponOfChoice(possibleWepsOfChoice.get(0));
        }

        Weapon finalStartingWeapon;
        if (possibleStartWeps.contains(startingWeapon)) {
            finalStartingWeapon = startingWeapon;
        } else {
            finalStartingWeapon = possibleStartWeps.get(0);
        }

        switch (finalStartingWeapon.getResId()) {
            case R.string.bow:
                startGear.add(equipmentDB.getWeapon(R.string.bow));
                startGear.add(equipmentDB.getWeapon(R.string.arrows));
                break;
            case R.string.javelin:
                startGear.add(equipmentDB.getWeapon(R.string.javelin));
                break;
            case R.string.sling:
                startGear.add(equipmentDB.getWeapon(R.string.sling));
                startGear.add(equipmentDB.getWeapon(R.string.slingshot));
                break;
        }// Equipment done

        int rolledGold = 0;

        startGear.add(equipmentDB.getWeapon(R.string.dagger));
        startGear.add(equipmentDB.getWeapon(R.string.spear));

        setBasicHits(12);
        setCharacter(playerCharacter);
        setPrimaryAttributes(primAttributes);
        setResId(Classes.CENTAUR.getResId());
        setRequiredGender(Gender.MALE);
        setStartGold(rolledGold);
        setStartGear(startGear);
    }

    @Override
    public void doLevelUp() {
        Score[] possibleScores = {Score.SKILL, Score.WILL, Score.MIGHT, Score.WITS};
        doLevelUp(possibleScores[Util.roll(possibleScores.length) - 1]);
    }

    @Override
    public void doLevelUp(Score score) {
        if (getLevel() < getEffectiveLevel()) {
            Score[] choices = {Score.SKILL, Score.WILL, Score.MIGHT, Score.WITS};
            ArrayList<Score> possibleScores = new ArrayList<>();
            for (Score selectScore: choices) {
                if(getCharacter().canAddToScore(selectScore)) {
                    possibleScores.add(selectScore);
                }
            }

            Score selectedScore;
            if (possibleScores.contains(score)) {
                selectedScore = score;
            } else {
                selectedScore = possibleScores.get(Util.roll(possibleScores.size()) - 1);
            }

            if (possibleScores.size() > 0) {
                while (!getCharacter().canAddToScore(selectedScore)) {
                    selectedScore = possibleScores.get((possibleScores.indexOf(selectedScore) + 1) % possibleScores.size());
                }
            }

            // Contains information about changed scores
            HashMap<Score, Integer> levelData = new HashMap<>();

            if (getCharacter().canAddToScore(Score.LUCK)) {
                AttributeScore luck = getCharacter().getScore(Score.LUCK);
                levelData.put(Score.LUCK, luck.getScore());
                luck.setScore(luck.getScore() + 1);
            } else {
                levelData.put(Score.LUCK, 20);
            }

            AttributeScore selectedAttrScore = getCharacter().getScore(selectedScore);

            levelData.put(selectedScore, selectedAttrScore.getScore());
            getScoreLevelChoice().add(levelData);

            selectedAttrScore.setScore(selectedAttrScore.getScore() + 2);
            setAddedHits(getAddedHits() + 4);

            setLevel(getLevel() + 1);
            getCharacter().validateScores();
        }
    }

    @Override
    public void doLevelDown() {
        if (getLevel() > 1) {
            HashMap<Score, Integer> levelData = getScoreLevelChoice().remove(getScoreLevelChoice().size() - 1);
            Score lastSelectedScore = null;
            for (Object rawScore: levelData.keySet().toArray()) {
                Score score = (Score) rawScore;
                if (score != Score.LUCK) {
                    lastSelectedScore = score;
                    break;
                }
            }

            AttributeScore luck = getCharacter().getScore(Score.LUCK);
            AttributeScore lastScoreLeveled = getCharacter().getScore(lastSelectedScore);

            setAddedHits(getAddedHits() - 4);
            luck.setScore(levelData.get(Score.LUCK));
            lastScoreLeveled.setScore(levelData.get(lastSelectedScore));
            setLevel(getLevel() - 1);
        }
    }

    public ArrayList<HashMap<Score, Integer>> getScoreLevelChoice() {
        return mScoreLevelChoice;
    }

    public void setScoreLevelChoice(ArrayList<HashMap<Score, Integer>> scoreLevelChoice) {
        mScoreLevelChoice = scoreLevelChoice;
    }

    public int getExtraoridnaryAgilityBonus(){
        return getCharacter().getScore(Score.SKILL).getModifier();
    }
}
