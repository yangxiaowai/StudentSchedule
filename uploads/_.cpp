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
        // ʹ�ù�ϣ��ͳ��ÿ���ַ����ֵ�Ƶ��
        unordered_map<char, int> frequencyMap;
        for (char c : s) {
            frequencyMap[c]++;
        }

        // ����ϣ���е�Ԫ�ط���һ��vector�У�����Ƶ�ʽ�������
        vector<pair<char, int>> frequencyVector(frequencyMap.begin(), frequencyMap.end());
        sort(frequencyVector.begin(), frequencyVector.end(), [](const pair<char, int>& a, const pair<char, int>& b) {
            if (a.second == b.second) {
                return a.first < b.first; // Ƶ����ͬ����ASCII���˳��
            }
            return a.second > b.second; // ��Ƶ�ʽ���
            });

        // ��������ַ���
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

    // ���ļ�
    ifstream infile("in.txt");
    if (!infile.is_open()) {
        cerr << "�޷����ļ� in.txt" << endl;
        return 1;
    }

    // ��ȡ�ļ�����
    getline(infile, input);
    infile.close();

    // ���ú�����������
    string output = sol.frequencySort(input);
    cout << output << endl;

    return 0;
}

