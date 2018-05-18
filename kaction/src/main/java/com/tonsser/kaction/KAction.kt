package com.tonsser.kaction

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.disposables.CancellableDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.CancellationException

class InvalidInput : Throwable("KAction input is null")
class IsExecuting : Throwable("KAction is already executing")
class NotEnabled : Throwable("KAction not enabled")


class KAction<in InputType, OutputType> @JvmOverloads constructor(private val workFactory: (InputType) -> Observable<OutputType>,
                                                                  private val enabledIf: Observable<Boolean> = Observable.just(true)) {

    private var isEnabled = true
    private var isExecuting = false
    private val disposables = CompositeDisposable()

    // Exposed flags
    var isEmmitErrorOnEnabled: Boolean = true
    var isEmmitErrorOnExecuting: Boolean = true

    /*************************
     *    Exposed streams    *
     *************************/

    val executing: PublishSubject<Boolean> = PublishSubject.create()
    val enabled: PublishSubject<Boolean> = PublishSubject.create()
    val outputs: PublishSubject<OutputType> = PublishSubject.create()
    val errors: PublishSubject<Throwable> = PublishSubject.create()
    val cancel: PublishSubject<Throwable> = PublishSubject.create()

    init {
        listenToInternalChanges()
    }

    /***************************
     *    Exposed functions    *
     ***************************/

    fun dispose() {
        disposables.dispose()
    }

    fun execute(input: InputType) {
        if (!isEnabled()) return
        if (!isInputValid(input)) return
        if (isExecuting()) return
        executeWorkFactory(input)
    }

    fun cancel(exception: Throwable? = null){
        enabled.onNext(false)
        cancel.onNext(exception?:CancellationException())
    }

    private fun executeWorkFactory(input: InputType) {
        enabled.onNext(false)
        executing.onNext(true)
        disposables.add(workFactory.invoke(input)
                .subscribe({
                    executing.onNext(false)
                    outputs.onNext(it)
                    enabled.onNext(true)
                }, {
                    executing.onNext(false)
                    errors.onNext(it)
                    enabled.onNext(true)
                })
        )
    }

    /********************************
     *    Private implementations   *
     ********************************/

    private fun isInputValid(input: InputType): Boolean {
        if (input == null) {
            errors.onNext(InvalidInput())
            return false
        }
        return true
    }

    private fun isEnabled(): Boolean {
        if (!isEnabled && isEmmitErrorOnEnabled) {
            errors.onNext(NotEnabled())
        }
        return isEnabled
    }

    private fun isExecuting(): Boolean {
        if (isExecuting && isEmmitErrorOnExecuting) {
            errors.onNext(IsExecuting())
        }
        return isExecuting
    }

    private fun listenToInternalChanges() {
        disposables.add(enabledIf.subscribeOn(Schedulers.io())
                .subscribe {
                    // enabledIf is the consumer provided condition
                    // while the enabled is the internal conditional "flag"
                    enabled.onNext(it)
                })

        disposables.add(enabled.subscribeOn(Schedulers.io())
                .subscribe {
                    isEnabled = it
                })

        disposables.add(executing.subscribeOn(Schedulers.io())
                .subscribe {
                    isExecuting = it
                })
    }
}