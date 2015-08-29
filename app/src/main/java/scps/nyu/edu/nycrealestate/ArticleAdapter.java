package scps.nyu.edu.nycrealestate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// this adapter loads a listview of real estate news articles (using data from parse.com)
public class ArticleAdapter extends ArrayAdapter<String>
{
    private Context context;
    private List<String> strings;

    public ArticleAdapter(Context context, List<String> strings)
    {
        super(context, R.layout.articlelayout, strings);
        this.context = context;
        this.strings = new ArrayList<>();
        this.strings = strings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.articlelayout, parent, false);

        TextView your_first_text_view = (TextView) rowView.findViewById(R.id.heading);
        TextView your_second_text_view = (TextView) rowView.findViewById(R.id.summary);

        // ~ is an uncommon character to see in a news article
        String[] articleData = strings.get(position).split("~");
        your_first_text_view.setText(articleData[0]);
        your_second_text_view.setText(articleData[1]);

        return rowView;
    }
}
