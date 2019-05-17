package com.example.rotation_test1;

public class Rotation {

    public Rotation() {

    }

    public double[][] Rot3D(float om, float ph, float kp){
       double[][] Rx = {{1,0,0},{0,Math.cos(om),-Math.sin(om)},{0,Math.sin(om),Math.cos(om)}};
       double[][] Ry = {{Math.cos(ph),0,Math.sin(ph)},{0,1,0},{-Math.sin(ph),0,Math.cos(ph)}};
       double[][] Rz = {{Math.cos(kp),-Math.sin(kp),0},{Math.sin(kp),Math.cos(kp),0},{0,0,1}};
       double[][] result_1 = new double[3][3];
       double[][] result_2 = new double[3][3];
       for (int i=0;i<3;i++){
           for (int j=0;j<3;j++){
               for (int k=0;k<3;k++){
                   result_1[i][j] += Rx[i][k]*Ry[k][j];
               }
           }
       }
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    result_2[i][j] += result_1[i][k]*Rz[k][j];
                }
            }
        }

        return result_2;
    }

    public double[][] dRot3D_om(float om, float ph, float kp){
        double[][] Rx = {{1,0,0},{0,Math.cos(om),-Math.sin(om)},{0,Math.sin(om),Math.cos(om)}};
        double[][] Ry = {{Math.cos(ph),0,Math.sin(ph)},{0,1,0},{-Math.sin(ph),0,Math.cos(ph)}};
        double[][] Rz = {{Math.cos(kp),-Math.sin(kp),0},{Math.sin(kp),Math.cos(kp),0},{0,0,1}};

        double[][] dRx = {{0,0,0},{0,-Math.sin(om),-Math.cos(om)},{0,Math.cos(om),-Math.sin(om)}};
        double[][] dRy = {{-Math.sin(ph),0,Math.cos(ph)},{0,0,0},{-Math.cos(ph),0,-Math.sin(ph)}};
        double[][] dRz = {{-Math.sin(kp),-Math.cos(kp),0},{Math.cos(kp),-Math.sin(kp),0},{0,0,0}};

        double[][] om_result_1 = new double[3][3];
        double[][] om_result_2 = new double[3][3];
        double[][] ph_result_1 = new double[3][3];
        double[][] ph_result_2 = new double[3][3];
        double[][] kp_result_1 = new double[3][3];
        double[][] kp_result_2 = new double[3][3];
        double[][] result = new double[9][3];
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    om_result_1[i][j] += dRx[i][k]*Ry[k][j];
                    ph_result_1[i][j] += Rx[i][k]*dRy[k][j];
                    kp_result_1[i][j] += Rx[i][k]*Ry[k][j];
                }
            }
        }
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    om_result_2[i][j] += om_result_1[i][k]*Rz[k][j];
                    ph_result_2[i][j] += ph_result_1[i][k]*Rz[k][j];
                    kp_result_2[i][j] += kp_result_1[i][k]*dRz[k][j];
                }
            }
        }
        for (int i=0;i<3;i++){
            result[i] = om_result_2[i];
            result[i+3] = ph_result_2[i];
            result[i+6] = kp_result_2[i];
        }

        return result;
    }
}
