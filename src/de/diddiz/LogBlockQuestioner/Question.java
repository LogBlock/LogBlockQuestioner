package de.diddiz.LogBlockQuestioner;

import java.util.HashMap;
import org.bukkit.entity.Player;

class Question
{
	private String answer = null;
	private final HashMap<Integer, String> answers;
	private final String questionMessage;
	private final Player respondent;
	private final int respondentHash;
	private final long start;

	Question(Player respondent, String questionMessage, String[] answers) throws IllegalArgumentException {
		if (answers == null || answers.length < 2)
			throw new IllegalArgumentException();
		start = System.currentTimeMillis();
		this.respondent = respondent;
		respondentHash = respondent.getName().hashCode();
		this.questionMessage = questionMessage;
		this.answers = new HashMap<Integer, String>(answers.length);
		for (final String ans : answers)
			this.answers.put(ans.toLowerCase().hashCode(), ans);
	}

	synchronized String ask() throws InterruptedException, QuestionerException {
		final StringBuilder options = new StringBuilder();
		for (final String ans : answers.values())
			options.append("/" + ans + ", ");
		options.delete(options.length() - 2, options.length());
		respondent.sendMessage(questionMessage);
		respondent.sendMessage("- " + options + "?");
		this.wait();
		if (answer == null)
			throw new QuestionerException("Timed out");
		return answer;
	}

	synchronized boolean isExpired() {
		if (System.currentTimeMillis() - start > 300000) {
			notify();
			return true;
		}
		return false;
	}

	boolean isPlayerQuestioned(int playerNameHash) {
		return playerNameHash == respondentHash;
	}

	boolean isRightAnswer(int answerHash) {
		return answers.containsKey(answerHash);
	}

	synchronized void returnAnswer(int answerHash) {
		answer = answers.get(answerHash);
		notify();
	}
}
