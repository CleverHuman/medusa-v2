<template>
  <div class="profile-container dark-theme">
    <h1 class="welcome-title">Welcome, {{ userInfo.username }}</h1>

    <div class="info-section">
      <el-row class="info-row">
        <el-col :span="12" class="info-label">Username</el-col>
        <el-col :span="12" class="info-value">{{ userInfo.username }}</el-col>
      </el-row>
      <el-row class="info-row">
        <el-col :span="12" class="info-label">Current Point</el-col>
        <el-col :span="12" class="info-value">{{ userInfo.currentPoint }}</el-col>
      </el-row>
      <el-row class="info-row">
        <el-col :span="12" class="info-label">Current Level</el-col>
        <el-col :span="12" class="info-value">{{ userInfo.currentLevel }}</el-col>
      </el-row>
    </div>

    <h2 class="section-title">Loyalty Point</h2>
    <div class="level-container">
      <div
        v-for="(level, index) in levels"
        :key="index"
        class="level-item"
      >
        <!-- 第一行：金属色背景 -->
        <div
          class="level-header"
          :style="{ backgroundColor: levelColors[level.name] }"
        >
          {{ level.name }}
        </div>

        <!-- 第二行：内容区域 -->
        <div
          class="level-content"
          :style="{
            backgroundColor: userInfo.level === level.name ? levelColors[level.name] : '',
            color: userInfo.level === level.name ? textColors[level.name] : ''
          }"
        >
          <div class="level-points">{{ level.points }} Points</div>
          <div class="level-desc">{{ level.desc }}</div>
        </div>
      </div>
    </div>

    <div class="agreement-section">
      <h3 class="agreement-title">User Agreement</h3>
      <div class="agreement-content">
        <div v-for="(paragraph, index) in formattedText" :key="index">
          <h4 v-if="paragraph.isQuestion" class="question">{{ paragraph.text }}</h4>
          <p v-else class="answer">{{ paragraph.text }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {getMemberInfo} from "@/api/profile";

export default {
  name: 'Profile',
  computed: {
    formattedText() {
      return this.longText.split('\n').filter(p => p.trim()).map(p => {
        const isQuestion = p.trim().endsWith('?')
        return {
          text: p.trim(),
          isQuestion: isQuestion
        }
      })
    }
  },
  data() {
    return {
      userInfo: {
        username: 'Loading...',
        currentPoint: 0,
        currentLevel: 'Bronze'
      },
      levels: [
        { name: 'Bronze', points: '0', desc: 'Exclusive Discounts\n' +
            'Community support' },
        { name: 'Silver', points: '50', desc: '$10 Discount\n' +
            'Live chat support' },
        { name: 'Gold', points: '100', desc: '$20 Discount\n' +
            'Dedicated Account\n' +
            'Manager' },
        { name: 'Platinum', points: '200', desc: '3% Discount\n' +
            'Free shipping' },
        { name: 'Diamond', points: '500', desc: '5% Discount\n' +
            'Free shipping' }
      ],
      levelColors: {
        Bronze: '#cd7f32',  // 青铜色
        Silver: '#c0c0c0',  // 银色
        Gold: '#ffd700',     // 金色
        Platinum: '#e5e4e2', // 铂金色
        Diamond: '#b9f2ff'   // 钻石蓝
      },
      textColors: {
        Bronze: '#ffffff',  // 白色文字
        Silver: '#000000',  // 黑色文字
        Gold: '#000000',     // 黑色文字
        Platinum: '#000000',// 黑色文字
        Diamond: '#000000'  // 黑色文字
      },
      longText: `
What’s a loyalty program?
Our loyalty program rewards customers for choosing us as your preferred vendor. You’ll earn points based on your activity throughout the year, which will determine your loyalty level. Each year, your points reset, and different loyalty levels come with various privileges, including exclusive discounts and services. For more details, please refer to the information above.

How can I earn points?
There are several ways to earn points:
Complete the beginner’s package.
Make Purchases: Earn 1% of your total purchases from our Tor or Telegram shop in points.
Refer a Friend:
Share our Tor or Telegram shop with a friend: you’ll earn 5% of your friend’s total AUD spend on their first purchase in points.
Refer a friend to our Telegram Channel: Earn 1 point.

How long are my points valid for?
Points are valid until 31st December of each year. Your accumulated points will determine your loyalty level for the following year.

Can I use my points?
Currently TP points are only used to determine your loyalty level for the next year. However, we are working on a new feature called “Store Credit.” which will allow you to convert points into AUD value for additional discounts. Stay tuned for more information!

Can I cash out my points?
No, points cannot be cashed out.

Do I need to sign up to join the program?
Yes, you must sign up to participate in the program. While we understand some customers prefer to order as guests for anonymity, registering allows us to track your order history and volume, which is essential for the program.

How can I see my points?
You can view your current points on this page. These points reflect your earnings for the current year and will influence your level for the following year. Points are automatically added to your account with each purchase. Any extra points earned through other activities will be manually added by our staff after verification.

How can I get discounts?
We are currently working on automating this process. In the meantime, you will receive a unique discount code that only you can use. For more details, please reach out to our friendly staff via Telegram at @TheProfessionals_CS1
      `
    }
  },
  created() {
    this.fetchMemberInfo()
  },
  methods: {
    fetchMemberInfo() {
      getMemberInfo().then(response => {
        console.log(response)
        this.userInfo = response.data
      }).catch(error => {
        console.error('Error fetching profile:', error)
        this.$message.error('Failed to load profile information')
      })
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 0;
}

.dark-theme {
  background-color: #1a1a1a;
  color: #ffffff;
}

.welcome-title {
  font-size: 2.5rem;
  margin-bottom: 30px;
  color: #FFF;
}

.info-section {
  background-color: #2d2d2d;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.info-row {
  padding: 15px 0;
  border-bottom: 1px solid #444;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: bold;
  font-size: 16px;
}

.info-value {
  text-align: right;
  color: #cccccc;
  font-size: 16px;
}

.section-title {
  font-size: 24px;
  margin: 40px 0 20px;
  color: #FFF;
}
.level-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.level-item {
  background: #333;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.level-item:hover {
  transform: translateY(-3px);
}

.level-header {
  padding: 15px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #000;
}

.level-content {
  padding: 20px;
  background: #2d2d2d;
  text-align: center;
  transition: background-color 0.3s;
}

.level-points {
  font-size: 16px;
  margin-bottom: 10px;
  font-weight: bold;
}

.level-desc {
  font-size: 14px;
  line-height: 1.4;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .level-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 20px;
  }
  .level-container {
    grid-template-columns: 1fr;
  }
  .level-header {
    padding: 12px;
    font-size: 16px;
  }
  .level-content {
    padding: 15px;
  }
}
.agreement-section {
  background: #2d2d2d;
  border-radius: 8px;
  padding: 25px;
  margin-top: 30px;
}

.agreement-title {
  font-size: 1.5em;
  color: #d4af37;
  margin-bottom: 20px;
}

.agreement-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 15px;
}

.question {
  font-size: 1.1em;
  color: #ffffff;
  margin: 15px 0 8px;
  font-weight: bold;
}

.answer {
  font-size: 0.95em;
  color: #cccccc;
  line-height: 1.7;
  margin-bottom: 20px;
  white-space: pre-wrap;
  word-break: break-word;
  padding-left: 15px;
  border-left: 2px solid #444;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #1a1a1a;
}

::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .agreement-section {
    padding: 15px;
  }

  .question {
    font-size: 1em;
  }

  .answer {
    font-size: 0.9em;
    padding-left: 10px;
  }
}
</style>
