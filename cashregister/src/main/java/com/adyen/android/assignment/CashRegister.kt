package com.adyen.android.assignment

import com.adyen.android.assignment.money.Change
import java.util.Collections.min

/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */
class CashRegister(private val change: Change) {
    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param price The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return The change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */
    fun performTransaction(price: Long, amountPaid: Change): Change {

        if (price > amountPaid.total) throw TransactionException("Price is greater than paid amount")

        // add paid bills/coins to cash register
        amountPaid.getElements().forEach { change.add(it, amountPaid.getCount(it)) }

        var difference = amountPaid.total - price

        // price and paid amount are equal: no change is returned
        if (difference == 0L) return Change.none()

        val changeForReturn = Change()

        // loop through bills/coins in cash register in descending order
        change.getElements().sortedByDescending { it.minorValue }.forEach {

            // check
            if (difference >= it.minorValue) {

                // find how many of the bill/coin should be returned
                val countMaxNeeded = difference / it.minorValue
                val countCashRegister = change.getCount(it)
                val countForReturn = minOf(countMaxNeeded.toInt(), countCashRegister)

                if (countForReturn > 0) { // cash register has the bill/coin
                    changeForReturn.add(it, countForReturn) // add bills/coins to change for return
                    change.remove(it, countForReturn) // take bills/coins from cash register
                    difference -= countForReturn * it.minorValue // remaining difference
                }

            }

        }

        if (difference > 0){

            // remove added bills/coins before throwing exception
            amountPaid.getElements().forEach { change.remove(it, amountPaid.getCount(it)) }

            throw TransactionException("Required bill or coin not found in cash register")

        }

        return changeForReturn

    }

    class TransactionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

}
