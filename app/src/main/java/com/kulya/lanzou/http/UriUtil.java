package com.kulya.lanzou.http;

/*
项目名称： lanzou
创建人：黄大神
类描述：网络链接
创建时间：2019/8/6 21:09
*/
public class UriUtil {
    public static final String GETFORMHASH = "https://pc.woozooo.com/account.php?action=login&ref=/mydisk.php";
    public static final String LOGIN = "https://pc.woozooo.com/account.php";
    public static final String HHTPHEAD = "https://pc.woozooo.com/";
    public static final String HOME = "https://pc.woozooo.com/mydisk.php?item=files&action=index&folder_node=1&folder_id=0";
    public static final String GETFILEID = "https://pc.woozooo.com/doupload.php";
    public static final String SHAREHEAD = "https://www.lanzous.com/";
    public static final String DOWNFILEHEAD = "https://vip.d0.baidupan.com/file/";
    public static final String GETDOWNURI = "https://www.lanzous.com/ajaxm.php";

/*
https://vip.d0.baidupan.com?              AmRWaAw9ADEIAQI6Cj9XO1BvUGgFs1fVUsJVtAHjUN9Tt1S5CO8DsgftU4UHtgW7AdgELwUuBnwLeFt7AjhaMgJuVmQMDgA9CDoCaQpvV2ZQM1BjBWJXYFJsVWQBe1A3UyBUPghkA2EHYVM9B2oFMgF9BHEFKgY5Cz5bbQJvWmICLVYxDGkAewhvAmQKcVdjUDtQZQU5V2dSZVUyATxQZlNqVDUIbQNiB2FTNAdhBWABbwRlBWkGMQs6WzkCO1o/AmBWYwxoADUIawIwCj1XeFB+UC0FLldzUiBVIAE4UCNTOlRnCGgDbwdpUzQHYQU2AW8EJwUuBm0LYVs4AjhaZgIzVjQMZwBhCGoCZQpsV2NQPVBnBXRXc1IgVSMBYFBgU31UJQgzAzsHJlM4B2MFIgEuBHEFLQY5Cz1baAJoWm4CNlYxDGQAZAhsAmA=
        https://vip.d0.baidupan.com/file/?AmRWaAw9ADEIAQI6Cj9XO1BvUGgFs1fVUsJVtAHjUN9Tt1S5CO8DsgftU4UHtgW7AdgELwUuBnwLeFt7AjhaMgJuVmQMDgA9CDoCaQpvV2ZQM1BjBWJXYFJsVWQBe1A3UyBUPghkA2EHYVM9B2oFMgF9BHEFKgY5Cz5bbQJvWmICLVYxDGkAewhvAmQKcVdjUDtQZQU5V2dSZVUyATxQZlNqVDUIbQNiB2FTNAdhBWABbwRlBWkGMQs6WzkCO1o/AmBWYwxoADUIawIwCj1XeFB+UC0FLldzUiBVIAE4UCNTOlRnCGgDbwdpUzQHYQU2AW8EJwUuBm0LYVs4AjhaZgIzVjQMZwBhCGoCZQpsV2NQPVBnBXRXc1IgVSMBYFBgU31UJQgzAzsHJlM4B2MFIgEuBHEFLQY5Cz1baAJoWm4CNlYxDGQAZAhsAmA=
https://vip.d0.baidupan.com/file/?BmBbZQ4/ADEEDQc/AjcCbgc4DjZT5QaoBb5WtVS/VfpTfVR3DypTcwBwATFQbVE8U2wDWVBmUzUCOFtsAGxRaQYwWzQOZQBkBGcHdwIwAnAHYA44Uz4GMwU7VmlUMVVyUyNUcw9vUzUAZgFmUD1Rf1M5Az5QIFNgAjVbcgBvUWUGPls/DmsAZARkBzcCNwI0B2sOMlM9Bj0FOlY0VD5VMVNhVDsPYFM2AG8BZFAwUWFTMQMwUG1TNQJjW2kAclEkBn5beA51ACEEIQc0AiQCagc5DjZTNAY8BTNWYlQ6VWBTdVR3DztTagAzATFQOVFhUzwDMFA6U2UCN1tuAG9RZQYyWyIOdQAhBCIHbAJnAi0Hew5tU2AGcwU/VmBULlUhUyNUdA9vUzYAYwFhUDFRZFM5AzNQP1NjAjM=
https://vip.d0.baidupan.com/file/?BmBbZQ4/ADEEDQc/AjcCbgc4DjZT5QaoBb5WtVS/VfpTfVR3DypTcwBwATFQbVE8U2wDWVBmUzUCOFtsAGxRaQYwWzQOZQBkBGcHdwIwAnAHYA44Uz4GMwU7VmlUMVVyUyNUcw9vUzUAZgFmUD1Rf1M5Az5QIFNgAjVbcgBvUWUGPls/DmsAZARkBzcCNwI0B2sOMlM9Bj0FOlY0VD5VMVNhVDsPYFM2AG8BZFAwUWFTMQMwUG1TNQJjW2kAclEkBn5beA51ACEEIQc0AiQCagc5DjZTNAY8BTNWYlQ6VWBTdVR3DztTagAzATFQOVFhUzwDMFA6U2UCN1tuAG9RZQYyWyIOdQAhBCIHbAJnAi0Hew5tU2AGcwU/VmBULlUhUyNUdA9vUzYAYwFhUDFRZFM5AzNQP1NjAjM=
*/
  /*完成  task: 2   任务代号  创建文件夹
    parent_id:  父文件夹代号
    folder_name:  文件夹名称
    folder_description: 文件夹描述   */

  /*完成 task: 5   访问文件夹
    folder_id: -1  文件夹代号
    pg: 1        第几页*/

  /* task: 3    删文件夹
    folder_id: 888950    */

   /* task: 6    删文件
    file_id: 10784920     */

//    task: 22         生成下载连接
//    file_id: 10958842  文件id
}
