package com.android.launcher3.aiot;

import java.util.List;

public class DeviceDataEntry {

    /**
     * family_id : xxxx
     * family_name : 我的家
     * device_open_num : 1
     * air_quality : 优
     * temperature : 26
     * humidity : 55
     * home_status_list : [{"device_id":"yyyy","device_name":"洗衣机","brand_id":1,"type_id":2,"product_model":"F1221TDHi","home_status":"剩余60min","status_type":"time"},{"device_id":"zzzz","device_name":"落地灯","brand_id":1,"type_id":33,"product_model":"ZLL","home_status":"亮度60%","status_type":"status"}]
     * notify_list : ["洗衣机剩余70min","台灯已使用三小时","卧室空调已打开","洗衣机运行在除螨模式"]
     */

    private String family_id;
    private String family_name;
    private String device_open_num;
    private String air_quality;
    private String temperature;
    private String humidity;
    private List<HomeStatusListBean> home_status_list;
    private List<String> notify_list;

    public String getFamily_id() {
        return family_id;
    }

    public void setFamily_id(String family_id) {
        this.family_id = family_id;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getDevice_open_num() {
        return device_open_num;
    }

    public void setDevice_open_num(String device_open_num) {
        this.device_open_num = device_open_num;
    }

    public String getAir_quality() {
        return air_quality;
    }

    public void setAir_quality(String air_quality) {
        this.air_quality = air_quality;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public List<HomeStatusListBean> getHome_status_list() {
        return home_status_list;
    }

    public void setHome_status_list(List<HomeStatusListBean> home_status_list) {
        this.home_status_list = home_status_list;
    }

    public List<String> getNotify_list() {
        return notify_list;
    }

    public void setNotify_list(List<String> notify_list) {
        this.notify_list = notify_list;
    }

    public static class HomeStatusListBean {
        /**
         * device_id : yyyy
         * device_name : 洗衣机
         * brand_id : 1
         * type_id : 2
         * product_model : F1221TDHi
         * home_status : 剩余60min
         * status_type : time
         */

        private String device_id;
        private String device_name;
        private String brand_id;
        private String type_id;
        private String product_model;
        private String home_status;
        private String status_type;

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }

        public String getDevice_name() {
            return device_name;
        }

        public void setDevice_name(String device_name) {
            this.device_name = device_name;
        }

        public String getBrand_id() {
            return brand_id;
        }

        public void setBrand_id(String brand_id) {
            this.brand_id = brand_id;
        }

        public String getType_id() {
            return type_id;
        }

        public void setType_id(String type_id) {
            this.type_id = type_id;
        }

        public String getProduct_model() {
            return product_model;
        }

        public void setProduct_model(String product_model) {
            this.product_model = product_model;
        }

        public String getHome_status() {
            return home_status;
        }

        public void setHome_status(String home_status) {
            this.home_status = home_status;
        }

        public String getStatus_type() {
            return status_type;
        }

        public void setStatus_type(String status_type) {
            this.status_type = status_type;
        }
    }
}
