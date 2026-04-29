package com.billexplain.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.billexplain.app.adapters.ChatAdapter;
import com.billexplain.app.databinding.ActivityChatBinding;
import com.billexplain.app.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private String userName = "Maria";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userName = getIntent().getStringExtra("user_name");
        if (userName == null) userName = "User";

        setupRecycler();
        setupSuggestions();

        // Pre-load AI greeting
        String greeting = "Hi " + userName + "! Your April bill is ₱3,250, which is 15% higher than last month. How can I help? 😊";
        addMessage(greeting, ChatMessage.TYPE_AI);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSend.setOnClickListener(v -> sendUserMessage());
        binding.etChat.setOnEditorActionListener((v, actionId, event) -> {
            sendUserMessage();
            return true;
        });
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(lm);
        binding.rvChat.setAdapter(adapter);
    }

    private void setupSuggestions() {
        binding.chipWhy.setOnClickListener(v -> handleSuggestion("Why is it so high?"));
        binding.chipReduce.setOnClickListener(v -> handleSuggestion("Show me how to reduce consumption"));
        binding.chipGeneration.setOnClickListener(v -> handleSuggestion("What is generation charge?"));
    }

    private void handleSuggestion(String text) {
        addMessage(text, ChatMessage.TYPE_USER);
        hideSuggestions();
        showTypingThenReply(text);
    }

    private void sendUserMessage() {
        String text = binding.etChat.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        binding.etChat.setText("");
        addMessage(text, ChatMessage.TYPE_USER);
        hideSuggestions();
        showTypingThenReply(text);
    }

    private void showTypingThenReply(String userText) {
        // Typing indicator
        ChatMessage typing = new ChatMessage("...", ChatMessage.TYPE_AI);
        messages.add(typing);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        new Handler().postDelayed(() -> {
            // Remove typing
            int idx = messages.indexOf(typing);
            if (idx >= 0) {
                messages.remove(idx);
                adapter.notifyItemRemoved(idx);
            }
            String reply = getAIReply(userText);
            addMessage(reply, ChatMessage.TYPE_AI);
        }, 800);
    }

    private String getAIReply(String input) {
        String lower = input.toLowerCase();

        if (lower.contains("why") && (lower.contains("high") || lower.contains("increase"))) {
            return "Your AC ran 40% more due to hot weather in April. Here's what contributed:\n\n" +
                   "• Generation charge jumped ₱150 vs last month\n" +
                   "• AC usage accounts for ~35% of total consumption\n" +
                   "• Water heater usage also spiked this period\n\n" +
                   "This is typical during the summer months (March–May) in the Philippines. 🌡️";
        }
        if (lower.contains("reduce") || lower.contains("save") || lower.contains("lower")) {
            return "Here are proven steps to reduce your bill:\n\n" +
                   "Step 1 🌡️ Set AC to 26°C instead of 23°C\n" +
                   "   → Saves approximately ₱150/month\n\n" +
                   "Step 2 💡 Switch to LED bulbs\n" +
                   "   → Saves approximately ₱80/month\n\n" +
                   "Step 3 🔌 Unplug idle devices (TV, chargers)\n" +
                   "   → Saves approximately ₱80/month\n\n" +
                   "Step 4 🚿 Use a timer on your water heater\n" +
                   "   → Saves approximately ₱90/month\n\n" +
                   "Total potential savings: ₱400/month 💰";
        }
        if (lower.contains("generation")) {
            return "Generation charge is what you pay for the actual production of electricity.\n\n" +
                   "This month it's ₱1,200 — about 37% of your total bill.\n\n" +
                   "It covers the cost of power plants generating the electricity that reaches your home. This rate is set by power generation companies and approved by the ERC. ⚡";
        }
        if (lower.contains("transmission")) {
            return "Transmission charge (₱450) covers transporting electricity from power plants to your local grid through high-voltage lines. It's regulated by the National Grid Corporation of the Philippines (NGCP). 🔌";
        }
        if (lower.contains("distribution")) {
            return "Distribution charge (₱680) is what your local electric cooperative or utility charges for delivering electricity from the grid directly to your home. This maintains the local power lines and transformers in your area. 🏠";
        }
        if (lower.contains("due") || lower.contains("when") || lower.contains("deadline")) {
            return "Your April 2026 bill of ₱3,250 is due on April 27, 2026 — just 2 days away! ⚡\n\nYou can pay via:\n• GCash\n• Maya\n• BPI\n• Landbank\n• UnionBank\n• PayPal\n\nTap the Pay tab to settle it now.";
        }

        // Default
        return "Great question! Based on your billing data, your April 2026 bill of ₱3,250 breaks down as:\n\n" +
               "• Generation: ₱1,200\n• Transmission: ₱450\n• Distribution: ₱680\n• Other: ₱920\n\n" +
               "Is there a specific charge you'd like me to explain? 💡";
    }

    private void addMessage(String text, int type) {
        messages.add(new ChatMessage(text, type));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        binding.rvChat.post(() ->
                binding.rvChat.smoothScrollToPosition(Math.max(0, messages.size() - 1)));
    }

    private void hideSuggestions() {
        binding.scrollSuggestions.setVisibility(View.GONE);
    }
}
