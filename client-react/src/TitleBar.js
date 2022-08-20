function TitleBar(props) {
	const { accounts, selectedAccount, setSelectedAccount, portfolio, requestPortfolio } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div className="pm-block">
			<div style={{ display: "flex", alignItems: "center" }}>
				<div>
					<select name="accounts" id="accounts" value={selectedAccount != null ? selectedAccount : ""} onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>

				<button type="button" style={{ marginLeft: 12 }} onClick={() => requestPortfolio(selectedAccount)}>Refresh</button>

				{
					portfolio && (
						<div>
							<span style={{ marginLeft: 12 }}>Balance: {portfolio.balance.toLocaleString()}</span>
							<span style={{ marginLeft: 12 }}>Cash: {portfolio.cash.toLocaleString()}</span>
							<span style={{ marginLeft: 24 }}>{new Date(portfolio.positionsTime).toLocaleString()}</span>
						</div>
					)
				}
			</div>
		</div>
	);
}

export default TitleBar;
