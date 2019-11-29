package com.example.my_sensor;

import android.widget.Toast;

public class path_alg {

    int MAX = 30000;
    int current_x;
    int current_y;
    int Min=MAX*MAX;
    int N = 11, M = 29;

    int[][] Visit = new int[11][29];
    int Dir[][] = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};


    char map[][] = {
            // 0  1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16  17  18  19  20  21 22  23  24  25  26  27  28
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','5','x','x','x','x','x','x','x','x'}, //0
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','x','x','x','x','x','x','x','x','x'}, //1
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','x','x','x','x','x','x','x','x','x'}, //2
            {'x','x','x','x','x','x','3','x','x','x','x','x','x','x','x','x','x','o','o','o','o','o','o','o','o','o','1','x','x'}, //3
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','x','x','x','o','o','o','o','o','o','o'}, //4
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //5
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //6
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //7
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //8
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','2','2'}, //9
            {'4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','x','x','x','x','x','x','s','x','x','x','x','x'}}; //10

    path_alg(int y, int x){
        current_x = x;
        current_y = y;
    }

    public int dfs_main(){
        dfs(current_y, current_x, 1);
        return Min;
    }

    public void dfs(int y, int x, int depth)
    {
        int i, j;
        int wX, wY;
        if(x<0 || y<0 || y>=N || x>=M)    //맵의 범위를 벗어 날때
            return;
        if(y == 0 && x == 20)    //도착할때
        {
            if(depth < Min)
                Min = depth;
            return;
        }

        for(i=0; i<4; i++)
        {
            wX = x + Dir[i][0];
            wY = y + Dir[i][1];
            if(wX<0 || wY<0 || wY>=N || wX>=M)    //맵의 범위를 벗어 날때
                continue;
            if(Visit[wY][wX] == 0 && map[wY][wX] != 'x')
            {
                Visit[wY][wX]  = 1;

                dfs(wY, wX, depth+1);
                Visit[wY][wX]  = 0;
            }
        }
    }
}
