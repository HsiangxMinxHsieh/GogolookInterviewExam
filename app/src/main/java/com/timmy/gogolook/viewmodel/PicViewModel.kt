package com.timmy.gogolook.viewmodel

import android.view.KeyEvent
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.timmy.gogolook.api.model.Hit
import timber.log.Timber
import java.util.*

class PicViewModel @ViewModelInject constructor(private val APIRepository: APIRepository, private val remoteRepository: RemoteRepository) : ViewModel() {

    /**是否正在載入中，用於顯示加載框 true=>Loading中，false=>載入完畢 */
    val observeIsLoading: ObservableField<Boolean> by lazy { ObservableField<Boolean>() }

    /**是否有搜尋到內容，用於顯示搜索結果是否有內容 true=>有內容，不顯示，false=>沒有內容，要顯示 */
    val observeHaveResult: ObservableField<Boolean> by lazy { ObservableField<Boolean>() }

    /**輸入框文字，用於雙向綁定EditText物件 */
    val observeContent: ObservableField<String> by lazy { ObservableField<String>() }

    /**搜尋紀錄列表，用於儲存與更新搜尋紀錄 */
    val liveSearchRecord: MutableLiveData<TreeSet<String>> by lazy { MutableLiveData<TreeSet<String>>() }

    /**關閉軟鍵盤通知，用於通知Activity關閉軟鍵盤*/
    val liveHideKeyBoard: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**訊息通知，用於通知Activity顯示Toast訊息 */
    val liveShowToast: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    /**通知View釋放Edit的Focus */
    val liveShowReleaseFocus: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    init {
        getDefaultData() // 初始化時取得資料
        liveSearchRecord.postValue(TreeSet()) // 初始化塞值(不然裡面是null)
    }

    fun getLiveDataByAPI(): LiveData<MutableList<Hit>> = APIRepository.getLiveDataByAPI()

    /**LayoutType，List與Grid。*/
    fun getLiveLayoutType(): LiveData<Boolean> = remoteRepository.getLiveLLayoutType()

    /**供View設定LayoutType(List、Grid)*/
    fun setLayoutType(isList: Boolean) {
        remoteRepository.getLiveLLayoutType().postValue(isList)
    }

    /**bonus，可以遙控GridLayout的行數(雖然有監聽，但在View處註解)*/
    fun getLiveLGridLayoutCount(): LiveData<Int> = remoteRepository.getLiveLGridLayoutCount()

    private fun getDefaultData() {
        showGetAPIScreen()
        APIRepository.getDefaultDataFromAPI()
        remoteRepository.fetchConfig()
    }

    /**搜尋方法*/
    fun search() {
        liveHideKeyBoard.postValue(true) // 先隱藏鍵盤再執行搜尋。
        showGetAPIScreen()

        // 開始執行搜尋動作
        val searchString = observeContent.get() ?: return
        observeContent.set("") // 清空搜尋文字(讓下次按下去會顯示搜尋紀錄)

        if (searchString.isBlank()) { // 如果是空字串搜尋，提示使用者沒有輸入，使用預設資料顯示畫面。
            liveShowToast.postValue("Empty input, use the default value to display.")
        }else{ // 不適空字串才要新增搜尋結果
            liveSearchRecord.postValue(liveSearchRecord.value?.apply { add(searchString) })
        }

        liveShowReleaseFocus.postValue(true)

        APIRepository.searchFromAPI(searchString)
    }

    /**設定跟API溝通中的畫面*/
    private fun showGetAPIScreen() {
        observeIsLoading.set(true)
    }

    /**Enter可以直接搜尋 為了要兼容模擬器時電腦鍵盤可以用，花費不少功夫在嘗試...Orz*/
    val editorActionListener: TextView.OnEditorActionListener = TextView.OnEditorActionListener { _, actionId, event ->
        if ((actionId == KeyEvent.KEYCODE_UNKNOWN || actionId == KeyEvent.KEYCODE_CALL) && event?.action != KeyEvent.ACTION_DOWN) {
            search()
        }
        true
    }

    companion object {

        @JvmStatic
        @BindingAdapter("onEditorActionListener")
        fun bindOnEditorActionListener(editText: EditText, editorActionListener: TextView.OnEditorActionListener) {
            editText.setOnEditorActionListener(editorActionListener)
        }

    }

}