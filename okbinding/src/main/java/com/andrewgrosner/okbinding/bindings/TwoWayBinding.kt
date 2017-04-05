package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.TextView
import java.util.*

fun <Input, Output, Converter : BindingConverter<Input>, V : View>
        OneWayBinding<Input, Output, Converter, V>.twoWay() = TwoWayBindingExpression(this)

class TwoWayBindingExpression<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayBinding: OneWayBinding<Input, Output, Converter, V>) {
    fun toInput(
            viewRegister: ViewRegister<V, Output>,
            inverseSetter: (InverseSetter<Output>)) = TwoWayBinding(this, viewRegister, inverseSetter)
}


private typealias InverseSetter<T> = (T?) -> Unit

/**
 * Reverses the binding on a field to [View] and provides also [View] to Field support.
 */
class TwoWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val twoWayBindingExpression: TwoWayBindingExpression<Input, Output, Converter, V>,
        val viewRegister: ViewRegister<V, Output>,
        inverseSetter: InverseSetter<Output>,
        val oneWayBinding: OneWayBinding<Input, Output, Converter, V> = twoWayBindingExpression.oneWayBinding)
    : Binding<Input, Output, Converter> {

    private val inverseSetters = mutableSetOf<InverseSetter<Output>>()

    init {
        viewRegister.register(oneWayBinding.view!!, { notifyViewChanged(it) })
        inverseSetters += inverseSetter
    }

    fun onExpression(inverseSetter: InverseSetter<Output>) = apply {
        inverseSetters += inverseSetter
    }

    override fun unbind() {
        oneWayBinding.unbind()
        viewRegister.deregister(oneWayBinding.view!!)
    }

    /**
     * Reruns binding expressions to views.
     */
    override fun notifyValueChange() {
        oneWayBinding.notifyValueChange()
    }

    /**
     * When view changes, call our binding expression again.
     */
    fun notifyViewChanged(value: Output?) {
        inverseSetters.forEach { it.invoke(value) }
    }
}

/**
 * Immediately binds changes from this [TextView] to the specified observable field in a two way binding.
 * Changes from either the view or the field are synchronized between each instance.
 */
fun TwoWayBindingExpression<String, String,
        ObservableBindingConverter<String>, TextView>.toFieldFromText()
        = toInput(TextViewRegister(), {
    val observableField = oneWayBinding.oneWayExpression.binding.observableField
    observableField.value = it ?: observableField.defaultValue
})

/**
 * Immediately binds changes from this [TextView] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<String, String,
        ObservableBindingConverter<String>, TextView>.toFieldExprFromText(inverseSetter: InverseSetter<String?>)
        = toInput(TextViewRegister(), inverseSetter)


/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field are synchronized between each instance.
 */
fun TwoWayBindingExpression<Boolean, Boolean, ObservableBindingConverter<Boolean>, CompoundButton>.toFieldFromCompound()
        = toInput(OnCheckedChangeRegister(), {
    val observableField = oneWayBinding.oneWayExpression.binding.observableField
    observableField.value = it ?: observableField.defaultValue
})

/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<Boolean, Boolean, ObservableBindingConverter<Boolean>, CompoundButton>.toFieldExprFromCompound(inverseSetter: InverseSetter<Boolean>)
        = toInput(OnCheckedChangeRegister(), inverseSetter)

/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field are synchronized between each instance.
 */
fun TwoWayBindingExpression<Calendar, Calendar, ObservableBindingConverter<Calendar>, DatePicker>.toFieldFromDate()
        = toInput(DatePickerRegister(oneWayBinding.convert()), {
    val observableField = oneWayBinding.oneWayExpression.binding.observableField
    observableField.value = it ?: observableField.defaultValue
})

/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<Calendar, Calendar, ObservableBindingConverter<Calendar>, DatePicker>.toFieldExprFromDate(inverseSetter: InverseSetter<Calendar>)
        = toInput(DatePickerRegister(oneWayBinding.convert()), inverseSetter)