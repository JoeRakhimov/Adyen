package com.adyen.android.assignment

import com.adyen.android.assignment.money.Bill
import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.Coin
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CashRegisterTest {

    private val changeInCashRegister = Change()
        .add(Bill.FIVE_EURO, 1)
        .add(Coin.FIFTY_CENT, 2)
    private val cashRegister = CashRegister(changeInCashRegister)

    @Test
    fun testTransaction_correctChangeReturned() {
        val price = 650L
        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 1)
        val expected = Change()
            .add(Coin.FIFTY_CENT, 1)
        val actual = cashRegister.performTransaction(price, amountPaid)
        assertEquals(expected, actual)
    }

    @Test
    fun testTransaction_correctChangeWithTwoOrMoreElementsReturned() {
        val price = 400L
        val amountPaid = Change()
            .add(Bill.TEN_EURO, 1)
        val expected = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.FIFTY_CENT, 2)
        val actual = cashRegister.performTransaction(price, amountPaid)
        assertEquals(expected, actual)
    }

    @Test
    fun testTransaction_correctAmountLeftInCashRegister() {
        val price = 650L
        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 1)
        cashRegister.performTransaction(price, amountPaid)
        val expected = Change()
            .add(Bill.FIVE_EURO, 2)
            .add(Coin.TWO_EURO, 1)
            .add(Coin.FIFTY_CENT, 1)
        val actual = changeInCashRegister
        assertEquals(expected, actual)
    }

    @Test
    fun priceAndPaidAmountAreEqual_noChangeReturned() {
        val price = 650L
        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.ONE_EURO, 1)
            .add(Coin.FIFTY_CENT, 1)
        val expected = Change.none()
        val actual = cashRegister.performTransaction(price, amountPaid)
        assertEquals(expected, actual)
    }

    @Test
    fun amountPaidIsLessThanPrice_throwsTransactionExceptionThatPriceIsGreaterThanPaidAmount(){
        val price = 650L
        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TEN_CENT, 3)
        val exception = assertThrows(CashRegister.TransactionException::class.java) {
            cashRegister.performTransaction(price, amountPaid)
        }
        val expected = "Price is greater than paid amount"
        val actual = exception.message
        assertEquals(expected, actual)
    }

    @Test
    fun notEnoughCoinInCashRegister_throwsTransactionExceptionThatRequiredBillOrCoinNotFound(){
        val price = 690L
        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 2)
        val exception = assertThrows(CashRegister.TransactionException::class.java) {
            cashRegister.performTransaction(price, amountPaid)
        }
        val expected = "Required bill or coin not found in cash register"
        val actual = exception.message
        assertEquals(expected, actual)
    }

}
