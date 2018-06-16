package com.example.a81266.minaweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a81266.minaweather.db.City;
import com.example.a81266.minaweather.db.County;
import com.example.a81266.minaweather.db.Province;
import com.example.a81266.minaweather.util.HttpUtil;
import com.example.a81266.minaweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private Button bt_return;
    private TextView tv_title;
    private ListView lv_area;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        bt_return = view.findViewById(R.id.bt_return);
        tv_title = view.findViewById(R.id.tv_title);
        lv_area = view.findViewById(R.id.lv_area);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        lv_area.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_area.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        bt_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        tv_title.setText("城市管理");
        bt_return.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv_area.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    private void queryCities() {
        tv_title.setText(selectedProvince.getProvinceName());
        bt_return.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv_area.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china"+"/"+ provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounties() {
        tv_title.setText(selectedCity.getCityName());
        bt_return.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv_area.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china"+"/"+ provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
               String  responseText = response.body().string();
               boolean result = false;
               if ("province".equals(type)){
                   result = Utility.handleProvinceResponse(responseText);
               }else if ("city".equals(type)){
                   result = Utility.handleCityResponse(responseText,selectedProvince.getId());
               }else if ("county".equals(type)){
                   result = Utility.handleCountyResponse(responseText,selectedCity.getId());
               }
               if (result){
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           closeProgressDialog();
                           if ("province".equals(type)){
                               queryProvinces();
                           }else if ("city".equals(type)){
                               queryCities();
                           }else if ("county".equals(type)){
                               queryCounties();
                           }
                       }
                   });
               }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
