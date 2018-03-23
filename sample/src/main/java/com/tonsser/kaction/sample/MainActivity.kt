package com.tonsser.kaction.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.tonsser.kaction.KAction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Creating an action which depending on the input (clicked item id) returns a message
        val sampleAction: KAction<Int, String> = KAction({
            val message = when (it) {
                R.id.navigation_home -> "Home"

                R.id.navigation_dashboard -> "Dashboard"

                R.id.navigation_notifications -> "Notifications"

                else -> "Undefined"
            }
            Observable.just(message)
            
        }, RxCompoundButton.checkedChanges(checkbox).subscribeOn(AndroidSchedulers.mainThread()) /* Optional: Only execute if checkbox is checked */)

        // Subscribe to outputs, set the text depending on the returned message
        compositeDisposable.add(sampleAction.outputs.subscribe {
            message.text = it
        })

        // Subscribe to errors
        compositeDisposable.add(sampleAction.errors.subscribe {
            message.text = it.message
        })

        // Subscribe to the executing state, this is where would normally attach a ProgressView's visibility to
        // a long running action.
        compositeDisposable.add(sampleAction.executing.subscribe {
            Toast.makeText(this, "Is executing $it", Toast.LENGTH_SHORT).show()
        })

        // The BottomNavigationView will return execute the sampleAction with the id of the clicked item
        navigation.setOnNavigationItemSelectedListener({
            sampleAction.execute(it.itemId)
            return@setOnNavigationItemSelectedListener true
        })
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
