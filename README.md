The Linda System
===

###### tags:`NCTUOS`

# 伺服器
ec2-52-70-79-11.compute-1.amazonaws.com

user: ubuntu



# 關於Linda
Linda 是一個小說系統?用來溝通協調處理程序對共享記憶體的**資料操作**

[Linda official website](http://lindaspaces.com/products/linda.html)

[LinuxTuples](https://sourceforge.net/projects/linuxtuples/
)

[Simple C-Linda]( https://www.comp.nus.edu.sg/~wongwf/linda.html)

## What is Tuples ?

在Linda 裡面 處理程序(Process) 透過Tuple space 溝通

TS 是一個全球共享的資料庫 管理被所有處理程序插入的Tuples

Tuples是有很多fields的向量 fields 裡面存有各種type 的資料

所有Tuples可以被所有Linda裡面的處理程序 **讀** 以及 **取得**

最原始的 Linda 模型會有以下四種操作

    1.In() Out(): 把tuple 放進 / 移出 tuple space

    2.Eval(): 產生一個新的程序 表現一些TS裡面的任務

    3.Rd(): 在不覆蓋的情況下 讀取 tuple的資料

![Tuple Space 大概長這樣](https://i.imgur.com/3I1or6m.jpg)



## 作業系統作業三

實做一個 Linda server 來儲存 tuples client 來 存放 tuples

並使用 Apache ActiveMQ (一個 Message Oriented Middleware)

來建立 server 和 client 之間的連結

### 條件設定

1. 資料型態有兩種  一種是字串(會用""包住) 一種是整數
2. 操作格式
    `[in/out/read] [value1] [value2]`
    
    每一個 field 中間有空格 
    
    field數量要比200少
    
    每一個operation 會比1024個字元少
    
    要可以支援以下例子
    `out “abc” 2 5`
    `read “abc” 2 5`
    `in “abc” 2 5`
    
    `in “abc” 2 ?i`
    `read “abc” 2 ?i`
    
    我們輸入的 field 又被稱做 template
    
    以下三個情況都符合的時候 match 會發生
    
    *   template 和 tuple field 數量相同
    *   在 template 中的 constant 和 variable 與 tuple 相同
    *   在 corresponding field 中的格式相同

   當一個process 沒有找到 match 的 tuple 
   
   她會暫停直到有其他程序插入對應的 tuple 
   
   原本暫停的程序在這同時會自動取得新的 tuple
   
   => 代表說 in/out 不管甚麼順序都可以
   
   只是 如果 先in 再 out 會造成一下下 delay
