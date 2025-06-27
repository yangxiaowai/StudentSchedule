#include <iostream>
#include <fstream>
#include <unordered_map>
#include <vector>
#include <algorithm>
#include <string>

using namespace std;

class Solution {
public:
    string frequencySort(string s) {
        // 使用哈希表统计每个字符出现的频率
        unordered_map<char, int> frequencyMap;
        for (char c : s) {
            frequencyMap[c]++;
        }

        // 将哈希表中的元素放入一个vector中，并按频率降序排序
        vector<pair<char, int>> frequencyVector(frequencyMap.begin(), frequencyMap.end());
        sort(frequencyVector.begin(), frequencyVector.end(), [](const pair<char, int>& a, const pair<char, int>& b) {
            if (a.second == b.second) {
                return a.first < b.first; // 频率相同，按ASCII码表顺序
            }
            return a.second > b.second; // 按频率降序
            });

        // 构建结果字符串
        string result;
        for (const auto& pair : frequencyVector) {
            result.append(pair.second, pair.first);
        }

        return result;
    }
};

int main() {
    Solution sol;
    string input;

    // 打开文件
    ifstream infile("in.txt");
    if (!infile.is_open()) {
        cerr << "无法打开文件 in.txt" << endl;
        return 1;
    }

    // 读取文件内容
    getline(infile, input);
    infile.close();

    // 调用函数并输出结果
    string output = sol.frequencySort(input);
    cout << output << endl;

    return 0;
}

