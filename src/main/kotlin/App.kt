import java.awt.*
import javax.swing.*
import kotlin.random.Random
import java.io.File

const val BALANCE_FILE = "balance.txt"

class CasinoApp : JFrame() {
    private var balance: Double = loadBalance()
    private val balanceLabel = JLabel("Current Balance: $$balance")
    private val betField = JTextField(10)
    private val resultLabel = JLabel("")

    init {
        title = "Kotlin Console Casino"
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = FlowLayout()

        add(balanceLabel)

        val kenoButton = JButton("Play Keno").apply {
            addActionListener { playKeno() }
        }
        add(kenoButton)

        val blackjackButton = JButton("Play Blackjack").apply {
            addActionListener { playBlackjack() }
        }
        add(blackjackButton)

        add(JLabel("Enter Bet Amount:"))
        add(betField)

        add(resultLabel)

        val exitButton = JButton("Exit").apply {
            addActionListener {
                saveBalance(balance)
                JOptionPane.showMessageDialog(this, "Thanks for playing! Your final balance is: $$balance")
                System.exit(0)
            }
        }
        add(exitButton)

        setSize(300, 200)
        isVisible = true
    }

    private fun loadBalance(): Double {
        return try {
            File(BALANCE_FILE).readText().toDouble()
        } catch (e: Exception) {
            println("Could not load balance. Starting with $100.0")
            100.0
        }
    }

    private fun saveBalance(balance: Double) {
        File(BALANCE_FILE).writeText(balance.toString())
    }

    private fun playKeno() {
        val input = JOptionPane.showInputDialog("Choose up to 10 numbers (1-80), separated by commas.")
        val chosenNumbers = input?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 1..80 } ?: emptyList()

        if (chosenNumbers.size > 10) {
            resultLabel.text = "You can only choose up to 10 numbers."
            return
        }

        val winningNumbers = generateWinningNumbers()
        resultLabel.text = "Winning numbers are: $winningNumbers"

        val matches = chosenNumbers.intersect(winningNumbers).size
        val bet = betField.text.toDoubleOrNull()

        if (bet == null || bet <= 0) {
            resultLabel.text += "\nInvalid bet amount. Please enter a positive number."
            return
        }

        balance = when (matches) {
            0 -> {
                resultLabel.text += "\nYou lose your bet of $$bet."
                balance - bet
            }
            1 -> balance + 1.also { resultLabel.text += "\nYou win $1 for matching 1 number!" }
            2 -> balance + 2.also { resultLabel.text += "\nYou win $2 for matching 2 numbers!" }
            3 -> balance + 5.also { resultLabel.text += "\nYou win $5 for matching 3 numbers!" }
            4 -> balance + 10.also { resultLabel.text += "\nYou win $10 for matching 4 numbers!" }
            5 -> balance + 20.also { resultLabel.text += "\nYou win $20 for matching 5 numbers!" }
            6 -> balance + 50.also { resultLabel.text += "\nYou win $50 for matching 6 numbers!" }
            7 -> balance + 100.also { resultLabel.text += "\nYou win $100 for matching 7 numbers!" }
            8 -> balance + 200.also { resultLabel.text += "\nYou win $200 for matching 8 numbers!" }
            9 -> balance + 500.also { resultLabel.text += "\nYou win $500 for matching 9 numbers!" }
            10 -> balance + 1000.also { resultLabel.text += "\nJackpot! You win $1000 for matching all 10 numbers!" }
            else -> balance
        }

        balanceLabel.text = "Current Balance: $$balance"
    }

    private fun generateWinningNumbers(): Set<Int> {
        return (1..80).shuffled().take(20).toSet()
    }

    private fun playBlackjack() {
        // Prompt for bet amount
        val betInput = JOptionPane.showInputDialog("Enter your bet amount:")
        val bet = betInput?.toDoubleOrNull()

        // Validate the bet amount
        if (bet == null || bet <= 0 || bet > balance) {
            resultLabel.text = "Invalid bet amount. Please enter a positive number that does not exceed your balance."
            return
        }

        val playerHand = mutableListOf<Int>()
        val dealerHand = mutableListOf<Int>()

        // Deal initial cards
        repeat(2) {
            playerHand.add(dealCard())
            dealerHand.add(dealCard())
        }

        // Show hands
        val playerTotal = handTotal(playerHand)
        val dealerFirstCard = dealerHand.first()

        val result = StringBuilder()
        result.append("Your hand: ${playerHand.joinToString(", ")} (Total: $playerTotal)\n")
        result.append("Dealer shows: $dealerFirstCard\n")

        // Display hands before asking to hit or stand
        resultLabel.text = result.toString()

        // Prompt for hit or stand
        val option = JOptionPane.showInputDialog("Do you want to hit or stand? (h/s)").orEmpty()
        var playerBusted = false

        if (option == "h") {
            playerHand.add(dealCard())
            val newTotal = handTotal(playerHand)
            result.append("Your new hand: ${playerHand.joinToString(", ")} (Total: $newTotal)\n")
            if (newTotal > 21) {
                result.append("You bust! You lose your bet of $$bet.")
                playerBusted = true
            }
        }

        // Dealer's turn if player didn't bust
        if (!playerBusted) {
            while (handTotal(dealerHand) < 17) {
                dealerHand.add(dealCard())
            }
            result.append("Dealer's hand: ${dealerHand.joinToString(", ")} (Total: ${handTotal(dealerHand)})\n")
            val dealerTotal = handTotal(dealerHand)

            if (dealerTotal > 21) {
                result.append("Dealer busts! You win!")
                balance += bet
            } else if (playerTotal > dealerTotal) {
                result.append("You win!")
                balance += bet
            } else if (playerTotal < dealerTotal) {
                result.append("You lose!")
                balance -= bet
            } else {
                result.append("It's a tie!")
            }
        }

        balanceLabel.text = "Current Balance: $$balance"
        resultLabel.text = result.toString()
    }



    private fun dealCard(): Int {
        return Random.nextInt(1, 12) // Cards valued 1-11 (Ace can be 1 or 11)
    }

    private fun handTotal(hand: List<Int>): Int {
        var total = hand.sum()
        var aceCount = hand.count { it == 1 } // Count Aces
        while (total <= 11 && aceCount > 0) {
            total += 10 // Convert Ace from 1 to 11
            aceCount--
        }
        return total
    }
}

fun main() {
    SwingUtilities.invokeLater { CasinoApp() }
}
